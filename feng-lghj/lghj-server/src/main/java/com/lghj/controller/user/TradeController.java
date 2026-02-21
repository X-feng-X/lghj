package com.lghj.controller.user;

import com.lghj.context.BaseContext;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.pojo.entity.TradeDeal;
import com.lghj.service.ITradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/trade")
@Api(tags = "交易操作接口")
@Slf4j
@RequiredArgsConstructor
public class TradeController {

    private final ITradeService tradeService;

    /**
     * 下单
     *
     * @param symbol
     * @param direction
     * @param price
     * @param quantity
     * @return
     */
    @PostMapping("/order")
    @ApiOperation("创建委托单")
    public Result createOrder(
            @RequestParam String symbol,
            @RequestParam Short direction, // 1-买，2-卖
            @RequestParam double price,
            @RequestParam int quantity // 数量
    ) {
        Long userId = BaseContext.getCurrentId();
        log.info("创建委托单: userId={}, symbol={}, direction={}, price={}, quantity={}",
                userId, symbol, direction, price, quantity);
        TradeOrder order = tradeService.createOrder(userId, symbol, direction, price, quantity);
        return Result.success(order);
    }

    /**
     * 撤单
     *
     * @param orderId
     * @return
     */
    @PostMapping("/cancel")
    @ApiOperation("撤销委托单")
    public Result cancelOrder(
            @RequestParam Long orderId
    ) {
        Long userId = BaseContext.getCurrentId();
        log.info("撤销委托单: orderId={}, userId={}", orderId, userId);
        boolean result = tradeService.cancelOrder(orderId, userId);
        return result ? Result.success() : Result.error("撤销失败");
    }

    /**
     * 获取用户委托单列表
     *
     * @return
     */
    @GetMapping("/query_orders")
    @ApiOperation("获取用户委托单列表")
    public Result getUserOrders() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取用户委托单列表: userId={}", userId);
        List<TradeOrder> orders = tradeService.getUserOrders(userId);
        return Result.success(orders);
    }

    /**
     * 获取用户成交记录列表
     *
     * @return
     */
    @GetMapping("/query_deals")
    @ApiOperation("获取用户成交记录列表")
    public Result getUserDeals() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取用户成交记录列表: userId={}", userId);
        List<TradeDeal> deals = tradeService.getUserDeals(userId);
        return Result.success(deals);
    }
}
