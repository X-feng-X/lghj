package com.lghj.utils;

import com.lghj.pojo.entity.TradeOrder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建一个订单队列管理类，为每个股票维护一个独立的队列，实现撮合引擎性能优化
 */
@Slf4j
@Component
public class OrderQueueManager {

    // TODO 改用RocketMQ
    // 为每个股票维护一个独立的订单队列
    private final Map<String, BlockingQueue<TradeOrder>> orderQueues = new HashMap<>();

    // 为每个股票维护一个独立的撮合线程
    private final Map<String, ExecutorService> matchExecutors = new HashMap<>();

    // 设置订单处理器
    // 订单处理器，由外部注入
    @Setter
    private OrderProcessor orderProcessor;

    /**
     * 将订单加入对应股票的队列
     *
     * @param order 订单
     */
    public void addOrder(TradeOrder order) {

        String symbol = order.getSymbol();
        // 线程安全地获取或创建一个与特定 symbol 关联的 BlockingQueue，用于存放 TradeOrder（交易委托单）对象
        BlockingQueue<TradeOrder> queue = orderQueues.computeIfAbsent(symbol, k -> new LinkedBlockingQueue<>());

        // 确保对应股票的撮合线程已启动
        startMatchThread(symbol);

        try {
            queue.put(order);
            log.info("订单已加入队列，股票代码：{}，委托单号：{}", symbol, order.getOrderNo());
        } catch (InterruptedException e) {
            log.error("将订单加入队列失败，股票代码：{}，委托单号：{}", symbol, order.getOrderNo(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 启动对应股票的撮合线程
     *
     * @param symbol 股票代码
     */
    private void startMatchThread(String symbol) {

        // 检查是否已经存在该股票代码的执行器
        if (!matchExecutors.containsKey(symbol)) {
            // 创建一个单线程执行器，用于处理特定股票的撮合任务
            ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
                // 创建并配置线程
                Thread t = new Thread(r, "match-thread-" + symbol);  // 设置线程名称
                t.setDaemon(true);  // 设置为守护线程
                return t;
            });

            // 提交任务到执行器
            executor.submit(() -> {
                // 获取该股票的订单队列
                BlockingQueue<TradeOrder> queue = orderQueues.get(symbol);
                if (queue == null) {
                    return;  // 如果队列为空，直接返回
                }

                // 循环处理订单，直到线程被中断
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // 从队列中获取订单并处理
                        TradeOrder order = queue.take();
                        if (orderProcessor != null) {
                            orderProcessor.processOrder(order);
                        }
                    } catch (InterruptedException e) {
                        // 处理线程中断异常
                        log.info("撮合线程被中断，股票代码：{}", symbol);
                        Thread.currentThread().interrupt();  // 恢复中断状态
                        break;  // 退出循环
                    } catch (Exception e) {
                        // 处理处理订单时的其他异常
                        log.error("处理订单失败，股票代码：{}", symbol, e);
                    }
                }
            });

            // 将执行器保存到map中
            matchExecutors.put(symbol, executor);
            log.info("已启动股票 {} 的撮合线程", symbol);
        }
    }

    /**
     * 停止所有撮合线程
     */
    @PreDestroy
    public void shutdown() {
        for (Map.Entry<String, ExecutorService> entry : matchExecutors.entrySet()) {
            String symbol = entry.getKey();
            ExecutorService executor = entry.getValue();
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
                log.info("已停止股票 {} 的撮合线程", symbol);
            } catch (InterruptedException e) {
                log.error("停止撮合线程失败，股票代码：{}", symbol, e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        matchExecutors.clear();
        orderQueues.clear();
    }

    /**
     * 订单处理器接口，由外部实现
     */
    public interface OrderProcessor {
        void processOrder(TradeOrder order);
    }
}
