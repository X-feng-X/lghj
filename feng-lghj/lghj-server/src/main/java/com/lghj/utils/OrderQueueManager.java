package com.lghj.utils;

import com.lghj.pojo.entity.TradeOrder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 订单队列管理器 - 为每个股票维护独立的撮合线程
 *
 * <p>
 * 设计模式：线程池 + 任务提交模式
 *
 * <p>
 * 核心机制：
 * <ul>
 * <li>每个股票绑定一个独立的单线程执行器</li>
 * <li>新订单到达时，向对应执行器提交处理任务</li>
 * <li>同一股票的订单串行处理，保证顺序性</li>
 * <li>不同股票的订单并行处理，提高吞吐量</li>
 * </ul>
 *
 * <p>
 * 架构图：
 * <pre>
 *                    ┌─────────────────────────────────────┐
 *                    │         OrderQueueManager           │
 *                    └─────────────────────────────────────┘
 *                                    │
 *          ┌─────────────────────────┼─────────────────────────┐
 *          │                         │                         │
 *          ▼                         ▼                         ▼
 *   ┌─────────────┐          ┌─────────────┐          ┌─────────────┐
 *   │  股票A队列   │          │  股票B队列   │          │  股票C队列   │
 *   │ (单线程执行器)│          │ (单线程执行器)│          │ (单线程执行器)│
 *   │             │          │             │          │             │
 *   │ task1→task2 │          │ task1→task2 │          │ task1→task2 │
 *   │    ↓        │          │    ↓        │          │    ↓        │
 *   │  串行执行    │          │  串行执行    │          │  串行执行    │
 *   └─────────────┘          └─────────────┘          └─────────────┘
 *          │                         │                         │
 *          └─────────────────────────┼─────────────────────────┘
 *                                    │
 *                                    ▼
 *                           ┌─────────────────┐
 *                           │  OrderProcessor │
 *                           │   (成交处理)     │
 *                           └─────────────────┘
 * </pre>
 */
@Slf4j
@Component
public class OrderQueueManager {

    /**
     * 每个股票对应的单线程执行器 Key: 股票代码（如 "sh600000"） Value: 单线程执行器，保证同一股票的订单串行处理
     */
    private final Map<String, ExecutorService> matchExecutors = new ConcurrentHashMap<>();

    /**
     * 订单处理器回调接口 由 TradeServiceImpl 实现，处理具体的成交逻辑
     */
    @Setter
    private OrderProcessor orderProcessor;

    /**
     * 将订单添加到处理队列
     *
     * <p>
     * 处理流程：
     * <ol>
     * <li>获取或创建该股票的单线程执行器</li>
     * <li>向执行器提交订单处理任务</li>
     * <li>执行器保证同一股票的订单串行处理</li>
     * </ol>
     *
     * @param order 委托单
     */
    public void addOrder(TradeOrder order) {
        String symbol = order.getSymbol();

        // 获取或创建该股票的执行器
        ExecutorService executor = getOrCreateExecutor(symbol);

        // 向执行器提交任务
        // 单线程执行器会保证任务按提交顺序串行执行
        executor.submit(() -> {
            try {
                if (orderProcessor != null) {
                    orderProcessor.processOrder(order);
                }
            } catch (Exception e) {
                log.error("处理订单失败，股票代码：{}，委托单号：{}", symbol, order.getOrderNo(), e);
            }
        });

        log.info("订单任务已提交，股票代码：{}，委托单号：{}", symbol, order.getOrderNo());
    }

    /**
     * 获取或创建指定股票的执行器
     *
     * <p>
     * 使用 computeIfAbsent 保证线程安全的懒加载：
     * <ul>
     * <li>如果执行器已存在，直接返回</li>
     * <li>如果执行器不存在，创建新的单线程执行器</li>
     * </ul>
     *
     * @param symbol 股票代码
     * @return 该股票的单线程执行器
     */
    private ExecutorService getOrCreateExecutor(String symbol) {
        return matchExecutors.computeIfAbsent(symbol, k -> {
            // 创建单线程执行器
            ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "match-thread-" + k);
                t.setDaemon(true); // 设置为守护线程，主线程退出时自动结束
                return t;
            });
            log.info("创建股票 {} 的撮合执行器", k);
            return executor;
        });
    }

    /**
     * 停止所有撮合线程
     *
     * <p>
     * 在应用关闭时调用（@PreDestroy），优雅关闭所有执行器：
     * <ol>
     * <li>调用 shutdown() 停止接受新任务</li>
     * <li>等待5秒让已提交的任务执行完成</li>
     * <li>如果超时，调用 shutdownNow() 强制终止</li>
     * </ol>
     */
    @PreDestroy
    public void shutdown() {
        log.info("开始关闭所有撮合执行器，共 {} 个", matchExecutors.size());

        for (Map.Entry<String, ExecutorService> entry : matchExecutors.entrySet()) {
            String symbol = entry.getKey();
            ExecutorService executor = entry.getValue();

            executor.shutdown(); // 停止接受新任务
            try {
                // 等待已提交的任务执行完成
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // 超时后强制终止
                    executor.shutdownNow();
                    log.warn("股票 {} 的执行器超时强制关闭", symbol);
                } else {
                    log.info("股票 {} 的执行器已优雅关闭", symbol);
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("关闭执行器被中断，股票代码：{}", symbol, e);
            }
        }

        matchExecutors.clear();
        log.info("所有撮合执行器已关闭");
    }

    /**
     * 获取活跃股票数量
     *
     * @return 当前有执行器的股票数量
     */
    public int getActiveSymbolCount() {
        return matchExecutors.size();
    }

    /**
     * 检查指定股票是否有活跃的执行器
     *
     * @param symbol 股票代码
     * @return 是否有活跃执行器
     */
    public boolean hasActiveExecutor(String symbol) {
        ExecutorService executor = matchExecutors.get(symbol);
        return executor != null && !executor.isShutdown();
    }

    /**
     * 订单处理器接口
     *
     * <p>
     * 由 TradeServiceImpl 实现，定义订单处理的具体逻辑
     */
    public interface OrderProcessor {

        /**
         * 处理订单
         *
         * @param order 委托单
         */
        void processOrder(TradeOrder order);
    }
}
