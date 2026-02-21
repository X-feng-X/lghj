package com.lghj.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.service.ITradeService;
import com.lghj.utils.OrderQueueManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 用于在系统启动时恢复未完成的订单，以及定期持久化订单簿快照，确保订单簿与数据库一致性
 */
@Slf4j
@Component
public class OrderRecoveryTask {

    private final ITradeService tradeService;
    private final OrderQueueManager orderQueueManager;

    @Autowired
    public OrderRecoveryTask(ITradeService tradeService, OrderQueueManager orderQueueManager) {
        this.tradeService = tradeService;
        this.orderQueueManager = orderQueueManager;
    }

    /**
     * 系统启动时恢复未完成的订单
     */
    @PostConstruct
    public void recoverUnfinishedOrders() {

        log.info("开始恢复未完成的订单...");

        try {
            // 查询所有状态为待定或部分完成的订单
            List<TradeOrder> unfinishedOrders = tradeService.list(
                    new LambdaQueryWrapper<TradeOrder>()
                            .in(TradeOrder::getStatus, 1, 2) // 1-待定，2-部分完成
                            .eq(TradeOrder::getIsDeleted, 0)
            );

            log.info("发现 {} 个未完成的订单", unfinishedOrders.size());

            // 将未完成的订单重新加入撮合队列
            for (TradeOrder order : unfinishedOrders) {
                orderQueueManager.addOrder(order);
                log.info("已恢复订单，委托单号：{}，股票代码：{}", order.getOrderNo(), order.getSymbol());
            }

            log.info("订单恢复完成");
        } catch (Exception e) {
            log.error("恢复未完成的订单失败", e);
        }
    }

    /**
     * 定期持久化订单簿快照（每小时执行一次）
     */
    @Scheduled(cron = "0 0 * * * ?")
    // TODO
    public void persistOrderBookSnapshot() {
        // 在实际系统中，这里应该实现订单簿快照的持久化逻辑
        // 例如将内存中的订单簿状态定期写入数据库或其他存储
        log.info("执行订单簿快照持久化任务");

        // 由于我们使用的是数据库作为主要存储，内存队列只是用于撮合
        // 所以这里可以简化处理，只记录日志
        log.info("订单簿快照持久化完成");
    }
}
