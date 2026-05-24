package com.lghj.service.trade;

import com.lghj.pojo.entity.TradeOrder;

import java.math.BigDecimal;

public interface TradeDirectionStrategy {

    short direction();

    void reserve(TradeOrder order);

    void release(TradeOrder order, int untradedQuantity);

    boolean canExecute(TradeOrder order, BigDecimal currentPrice);

    void validateBeforeDeal(TradeOrder order, int dealQuantity);

    void settle(TradeOrder order, BigDecimal dealPrice, int dealQuantity);
}
