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
public class BuyTradeDirectionStrategy implements TradeDirectionStrategy {

    private final TradeAccountOperator accountOperator;

    @Override
    public short direction() {
        return 1;
    }

    @Override
    public void reserve(TradeOrder order) {
        SimAccount account = accountOperator.getAccount(order.getUserId());
        if (account == null) {
            throw new BusinessException(ErrorEnum.ACCOUNT_NOT_FOUND, "用户账户不存在");
        }

        BigDecimal frozenAmount = accountOperator.orderAmount(order.getPrice(), order.getQuantity());
        if (account.getAvailableCash().compareTo(frozenAmount) < 0) {
            throw new BusinessException(ErrorEnum.DONT_HAVE_ENOUGH_MONEY, "账户可用资金不足");
        }

        account.setFrozenCash(account.getFrozenCash().add(frozenAmount));
        account.setAvailableCash(account.getAvailableCash().subtract(frozenAmount));
        accountOperator.updateAccount(account);
    }

    @Override
    public void release(TradeOrder order, int untradedQuantity) {
        SimAccount account = accountOperator.getAccount(order.getUserId());
        if (account == null) {
            return;
        }

        BigDecimal unfreezeAmount = accountOperator.orderAmount(order.getPrice(), untradedQuantity);
        account.setFrozenCash(account.getFrozenCash().subtract(unfreezeAmount));
        account.setAvailableCash(account.getAvailableCash().add(unfreezeAmount));
        accountOperator.updateAccount(account);
    }

    @Override
    public boolean canExecute(TradeOrder order, BigDecimal currentPrice) {
        return order.getPrice().compareTo(currentPrice) >= 0;
    }

    @Override
    public void validateBeforeDeal(TradeOrder order, int dealQuantity) {
        SimAccount account = accountOperator.getAccount(order.getUserId());
        if (account == null) {
            throw new BusinessException(ErrorEnum.ACCOUNT_NOT_FOUND, "用户账户不存在");
        }
    }

    @Override
    public void settle(TradeOrder order, BigDecimal dealPrice, int dealQuantity) {
        Long userId = order.getUserId();
        String symbol = order.getSymbol();

        SimAccount account = accountOperator.getAccount(userId);
        if (account != null) {
            BigDecimal actualAmount = accountOperator.orderAmount(dealPrice, dealQuantity);
            BigDecimal frozenAmount = accountOperator.orderAmount(order.getPrice(), dealQuantity);
            account.setTotalCash(account.getTotalCash().subtract(actualAmount));
            account.setFrozenCash(account.getFrozenCash().subtract(frozenAmount));
            account.setAvailableCash(account.getAvailableCash().add(frozenAmount.subtract(actualAmount)));
            accountOperator.updateAccount(account);
        }

        UserPosition position = accountOperator.getPosition(userId, symbol);
        int stockQuantity = accountOperator.stockQuantity(dealQuantity);
        if (position == null) {
            position = UserPosition.builder()
                    .userId(userId)
                    .accountId(userId)
                    .symbol(symbol)
                    .totalQuantity(stockQuantity)
                    .frozenQuantity(0)
                    .availableQuantity(stockQuantity)
                    .costPrice(dealPrice)
                    .profitLoss(BigDecimal.ZERO)
                    .version(1)
                    .build();
            accountOperator.insertPosition(position);
            return;
        }

        int newTotalQuantity = position.getTotalQuantity() + stockQuantity;
        BigDecimal newCostPrice = position.getCostPrice().multiply(BigDecimal.valueOf(position.getTotalQuantity()))
                .add(dealPrice.multiply(BigDecimal.valueOf(stockQuantity)))
                .divide(BigDecimal.valueOf(newTotalQuantity), 2, BigDecimal.ROUND_HALF_UP);

        position.setTotalQuantity(newTotalQuantity);
        position.setAvailableQuantity(position.getAvailableQuantity() + stockQuantity);
        position.setCostPrice(newCostPrice);
        accountOperator.updatePosition(position);
    }
}
