package com.lghj.service.trade;

import com.lghj.enums.ErrorEnum;
import com.lghj.exception.BusinessException;
import com.lghj.pojo.entity.SimAccount;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.pojo.entity.UserPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SellTradeDirectionStrategy implements TradeDirectionStrategy {

    private final TradeAccountOperator accountOperator;

    @Override
    public short direction() {
        return 2;
    }

    @Override
    public void reserve(TradeOrder order) {
        UserPosition position = accountOperator.getPosition(order.getUserId(), order.getSymbol());
        int stockQuantity = accountOperator.stockQuantity(order.getQuantity());
        if (position == null || position.getAvailableQuantity() < stockQuantity) {
            throw new BusinessException(ErrorEnum.POSITION_NOT_ENOUGH, "持仓不足");
        }

        position.setFrozenQuantity(position.getFrozenQuantity() + stockQuantity);
        position.setAvailableQuantity(position.getAvailableQuantity() - stockQuantity);
        accountOperator.updatePosition(position);
    }

    @Override
    public void release(TradeOrder order, int untradedQuantity) {
        UserPosition position = accountOperator.getPosition(order.getUserId(), order.getSymbol());
        if (position == null) {
            return;
        }

        int unfreezeQuantity = accountOperator.stockQuantity(untradedQuantity);
        position.setFrozenQuantity(position.getFrozenQuantity() - unfreezeQuantity);
        position.setAvailableQuantity(position.getAvailableQuantity() + unfreezeQuantity);
        accountOperator.updatePosition(position);
    }

    @Override
    public boolean canExecute(TradeOrder order, BigDecimal currentPrice) {
        return order.getPrice().compareTo(currentPrice) <= 0;
    }

    @Override
    public void validateBeforeDeal(TradeOrder order, int dealQuantity) {
        UserPosition position = accountOperator.getPosition(order.getUserId(), order.getSymbol());
        int stockQuantity = accountOperator.stockQuantity(dealQuantity);
        if (position == null || position.getFrozenQuantity() < stockQuantity) {
            throw new BusinessException(ErrorEnum.POSITION_NOT_ENOUGH, "冻结持仓不足");
        }
    }

    @Override
    public void settle(TradeOrder order, BigDecimal dealPrice, int dealQuantity) {
        Long userId = order.getUserId();
        String symbol = order.getSymbol();

        SimAccount account = accountOperator.getAccount(userId);
        if (account != null) {
            BigDecimal totalAmount = accountOperator.orderAmount(dealPrice, dealQuantity);
            account.setAvailableCash(account.getAvailableCash().add(totalAmount));
            account.setTotalCash(account.getTotalCash().add(totalAmount));
            accountOperator.updateAccount(account);
        }

        UserPosition position = accountOperator.getPosition(userId, symbol);
        if (position == null) {
            return;
        }

        int stockQuantity = accountOperator.stockQuantity(dealQuantity);
        int newTotalQuantity = position.getTotalQuantity() - stockQuantity;
        if (newTotalQuantity <= 0) {
            accountOperator.deletePosition(position.getId());
            return;
        }

        position.setTotalQuantity(newTotalQuantity);
        position.setFrozenQuantity(position.getFrozenQuantity() - stockQuantity);
        accountOperator.updatePosition(position);
    }
}
