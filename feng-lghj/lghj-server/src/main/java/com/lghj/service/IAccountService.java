package com.lghj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.entity.SimAccount;
import com.lghj.pojo.entity.UserPosition;

import java.util.List;

public interface IAccountService extends IService<SimAccount> {

    /**
     * 为用户创建模拟账户
     * @param userId 用户ID
     * @return 模拟账户
     */
    SimAccount createAccount(Long userId);

    /**
     * 获取用户的模拟账户
     * @param userId 用户ID
     * @return 模拟账户
     */
    SimAccount getAccountByUserId(Long userId);

    /**
     * 获取用户的持仓列表
     * @param userId 用户ID
     * @return 持仓列表
     */
    List<UserPosition> getUserPositions(Long userId);

    /**
     * 获取用户对特定股票的持仓
     * @param userId 用户ID
     * @param symbol 股票代码
     * @return 持仓信息
     */
    UserPosition getUserPositionBySymbol(Long userId, String symbol);
}
