package com.lghj.service.trade;

import com.lghj.mapper.TradeDealMapper;
import com.lghj.mapper.TradeOrderMapper;
import com.lghj.pojo.entity.TradeDeal;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.utils.HybridOrderBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeDealExecutor {

    private final TradeDealMapper tradeDealMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final HybridOrderBook hybridOrderBook;
    private final TradeDirectionRouter directionRouter;

    public void execute(TradeOrder order, BigDecimal dealPrice, int dealQuantity) {
        TradeDirectionStrategy strategy = directionRouter.route(order.getDirection());
        strategy.validateBeforeDeal(order, dealQuantity);

        String dealNo = "DEAL" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        TradeDeal deal = TradeDeal.builder()
                .dealNo(dealNo)
                .orderId(order.getId())
                .userId(order.getUserId())
                .symbol(order.getSymbol())
                .dealDirection(order.getDirection())
                .price(dealPrice)
                .quantity(dealQuantity)
                .build();
        tradeDealMapper.insert(deal);

        order.setTradedQuantity(order.getTradedQuantity() + dealQuantity);
        if (order.getTradedQuantity() >= order.getQuantity()) {
            order.setStatus((short) 3);
            hybridOrderBook.removeOrder(order);
        } else {
            order.setStatus((short) 2);
        }
        tradeOrderMapper.updateById(order);

        strategy.settle(order, dealPrice, dealQuantity);
        log.info("Trade deal executed, dealNo={}, orderNo={}, symbol={}, price={}, quantity={}",
                dealNo, order.getOrderNo(), order.getSymbol(), dealPrice, dealQuantity);
    }
}
