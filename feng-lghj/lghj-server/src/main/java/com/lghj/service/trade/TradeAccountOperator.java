package com.lghj.service.trade;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lghj.enums.ErrorEnum;
import com.lghj.exception.BusinessException;
import com.lghj.mapper.SimAccountMapper;
import com.lghj.mapper.UserPositionMapper;
import com.lghj.pojo.entity.SimAccount;
import com.lghj.pojo.entity.UserPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TradeAccountOperator {

    public static final int LOT_SIZE = 100;

    private final SimAccountMapper simAccountMapper;
    private final UserPositionMapper userPositionMapper;

    public SimAccount getAccount(Long userId) {
        return simAccountMapper.selectById(userId);
    }

    public void updateAccount(SimAccount account) {
        int updateResult = simAccountMapper.updateById(account);
        if (updateResult == 0) {
            throw new BusinessException(ErrorEnum.ACCOUNT_UPDATE_FAIL, "更新账户失败，可能存在并发操作");
        }
    }

    public UserPosition getPosition(Long userId, String symbol) {
        LambdaQueryWrapper<UserPosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPosition::getUserId, userId)
                .eq(UserPosition::getSymbol, symbol)
                .eq(UserPosition::getIsDeleted, 0);
        return userPositionMapper.selectOne(queryWrapper);
    }

    public void insertPosition(UserPosition position) {
        userPositionMapper.insert(position);
    }

    public void updatePosition(UserPosition position) {
        int updateResult = userPositionMapper.updateById(position);
        if (updateResult == 0) {
            throw new BusinessException(ErrorEnum.POSITION_UPDATE_FAIL, "更新持仓失败，可能存在并发操作");
        }
    }

    public void deletePosition(Long positionId) {
        userPositionMapper.deleteById(positionId);
    }

    public BigDecimal orderAmount(BigDecimal price, int lotQuantity) {
        return price.multiply(BigDecimal.valueOf(stockQuantity(lotQuantity)));
    }

    public int stockQuantity(int lotQuantity) {
        return lotQuantity * LOT_SIZE;
    }
}
