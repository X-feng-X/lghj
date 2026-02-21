package com.lghj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.pojo.entity.TradeDeal;

import java.util.List;

public interface ITradeService extends IService<TradeOrder> {

    /**
     * 创建委托单
     *
     * @param userId    用户ID
     * @param symbol    股票代码
     * @param direction 交易方向（1-买，2-卖）
     * @param price     委托价格
     * @param quantity  委托数量
     * @return 委托单
     */
    TradeOrder createOrder(Long userId, String symbol, Short direction, double price, int quantity);

    /**
     * 撤销委托单
     *
     * @param orderId 委托单ID
     * @param userId  用户ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId, Long userId);

    /**
     * 分页查询成交记录
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @param symbol   股票代码
     * @return 分页结果
     */
    Page<TradeDeal> queryDealPage(Integer pageNum, Integer pageSize, Long userId, String symbol);

    /**
     * 获取用户的委托单列表
     *
     * @param userId 用户ID
     * @return 委托单列表
     */
    List<TradeOrder> getUserOrders(Long userId);

    /**
     * 获取用户的成交记录列表
     *
     * @param userId 用户ID
     * @return 成交记录列表
     */
    List<TradeDeal> getUserDeals(Long userId);
}
