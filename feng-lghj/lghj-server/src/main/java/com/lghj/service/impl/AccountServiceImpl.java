package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.mapper.SimAccountMapper;
import com.lghj.mapper.UserPositionMapper;
import com.lghj.pojo.entity.SimAccount;
import com.lghj.pojo.entity.UserPosition;
import com.lghj.service.IAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<SimAccountMapper, SimAccount> implements IAccountService {

    private static final BigDecimal INITIAL_CASH = new BigDecimal("200000.00");

    private final UserPositionMapper userPositionMapper;

    /**
     * 创建用户模拟账户
     */
    @Override
    public SimAccount createAccount(Long userId) {
        // 检查用户是否已有账户
        SimAccount existingAccount = getAccountByUserId(userId);
        if (existingAccount != null) {
            log.info("用户已有模拟账户，用户ID：{}", userId);
            return existingAccount;
        }

        // 创建新的模拟账户
        SimAccount account = SimAccount.builder()
                .userId(userId)
                .totalCash(INITIAL_CASH)
                .availableCash(INITIAL_CASH)
                .frozenCash(BigDecimal.ZERO)
                .totalAsset(INITIAL_CASH)
                .version(1) // 初始版本号，用于乐观锁
                .build();

        save(account);
        log.info("为用户创建模拟账户，用户ID：{}，初始资金：{}", userId, INITIAL_CASH);
        return account;
    }

    /**
     * 获取用户的模拟账户
     */
    @Override
    public SimAccount getAccountByUserId(Long userId) {
        LambdaQueryWrapper<SimAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimAccount::getUserId, userId)
                .eq(SimAccount::getIsDeleted, 0);
        return getOne(queryWrapper);
    }

    /**
     * 获取用户的持仓列表
     */
    @Override
    public List<UserPosition> getUserPositions(Long userId) {
        LambdaQueryWrapper<UserPosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPosition::getUserId, userId)
                .eq(UserPosition::getIsDeleted, 0);
        return userPositionMapper.selectList(queryWrapper);
    }

    /**
     * 获取用户对特定股票的持仓
     */
    @Override
    public UserPosition getUserPositionBySymbol(Long userId, String symbol) {
        LambdaQueryWrapper<UserPosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPosition::getUserId, userId)
                .eq(UserPosition::getSymbol, symbol)
                .eq(UserPosition::getIsDeleted, 0);
        return userPositionMapper.selectOne(queryWrapper);
    }
}
