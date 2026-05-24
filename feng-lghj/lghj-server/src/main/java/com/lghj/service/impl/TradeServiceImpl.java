package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.mapper.TradeDealMapper;
import com.lghj.mapper.TradeOrderMapper;
import com.lghj.pojo.entity.TradeDeal;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.service.IRealTimeStockService;
import com.lghj.service.ITradeService;
import com.lghj.service.trade.TradeDealExecutor;
import com.lghj.service.trade.TradeDirectionRouter;
import com.lghj.service.trade.TradeDirectionStrategy;
import com.lghj.utils.HybridOrderBook;
import com.lghj.utils.OrderQueueManager;
import com.lghj.utils.RedissonLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder>
        implements ITradeService, OrderQueueManager.OrderProcessor {

    private final TradeDealMapper tradeDealMapper;
    private final OrderQueueManager orderQueueManager;
    private final RedissonLockUtil redissonLockUtil;
    private final HybridOrderBook hybridOrderBook;
    private final IRealTimeStockService realTimeStockService;
    private final TradeDirectionRouter directionRouter;
    private final TradeDealExecutor tradeDealExecutor;

    @PostConstruct
    public void init() {
        orderQueueManager.setOrderProcessor(this);
        hybridOrderBook.setOrderProcessor(this);
        log.info("TradeService initialized with order queue and order book processors");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(Long userId, String symbol, Short direction, double price, int quantity) {
        TradeOrder order = TradeOrder.builder()
                .orderNo("ORDER" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .userId(userId)
                .symbol(symbol)
                .direction(direction)
                .price(BigDecimal.valueOf(price))
                .quantity(quantity)
                .tradedQuantity(0)
                .status((short) 1)
                .build();

        TradeDirectionStrategy strategy = directionRouter.route(direction);
        strategy.reserve(order);

        save(order);
        orderQueueManager.addOrder(order);
        log.info("Trade order created, orderNo={}, userId={}, symbol={}, direction={}, price={}, quantity={}",
                order.getOrderNo(), userId, symbol, direction, price, quantity);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(TradeOrder order) {
        try {
            log.info("Matching engine starts order, orderNo={}, symbol={}", order.getOrderNo(), order.getSymbol());
            processTrade(order);
            log.info("Matching engine finished order, orderNo={}, symbol={}", order.getOrderNo(), order.getSymbol());
        } catch (Exception e) {
            log.error("Matching engine failed order, orderNo={}, symbol={}", order.getOrderNo(), order.getSymbol(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, Long userId) {
        TradeOrder order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            log.warn("Cancel rejected, order not found or user mismatch, orderId={}, userId={}", orderId, userId);
            return false;
        }
        if (order.getStatus() == 3 || order.getStatus() == 4) {
            log.warn("Cancel rejected, order already finished, orderId={}, status={}", orderId, order.getStatus());
            return false;
        }

        int untradedQuantity = order.getQuantity() - order.getTradedQuantity();
        if (untradedQuantity <= 0) {
            return false;
        }

        String lockKey = RedissonLockUtil.generateTradeLockKey(userId, order.getSymbol());
        boolean locked = false;
        try {
            locked = redissonLockUtil.tryLock(lockKey, 5, 30);
            if (!locked) {
                log.warn("Cancel rejected, lock unavailable, orderId={}", orderId);
                return false;
            }

            order.setStatus((short) 4);
            order.setCancelTime(LocalDateTime.now());
            updateById(order);
            hybridOrderBook.removeOrder(order);
            directionRouter.route(order.getDirection()).release(order, untradedQuantity);

            log.info("Trade order canceled, orderId={}, userId={}", orderId, userId);
            return true;
        } finally {
            if (locked) {
                redissonLockUtil.unlock(lockKey);
            }
        }
    }

    @Override
    public List<TradeOrder> getUserOrders(Long userId) {
        LambdaQueryWrapper<TradeOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeOrder::getUserId, userId)
                .eq(TradeOrder::getIsDeleted, 0)
                .orderByDesc(TradeOrder::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<TradeDeal> getUserDeals(Long userId) {
        LambdaQueryWrapper<TradeDeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeDeal::getUserId, userId)
                .eq(TradeDeal::getIsDeleted, 0)
                .orderByDesc(TradeDeal::getCreateTime);
        return tradeDealMapper.selectList(queryWrapper);
    }

    @Override
    public Page<TradeDeal> queryDealPage(Integer pageNum, Integer pageSize, Long userId, String symbol) {
        Page<TradeDeal> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TradeDeal> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(TradeDeal::getUserId, userId);
        }
        if (StringUtils.hasText(symbol)) {
            queryWrapper.eq(TradeDeal::getSymbol, symbol);
        }
        queryWrapper.eq(TradeDeal::getIsDeleted, 0)
                .orderByDesc(TradeDeal::getCreateTime);
        return tradeDealMapper.selectPage(page, queryWrapper);
    }

    private void processTrade(TradeOrder order) {
        String lockKey = RedissonLockUtil.generateTradeLockKey(order.getUserId(), order.getSymbol());
        boolean locked = false;
        try {
            locked = redissonLockUtil.tryLock(lockKey, 5, 30);
            if (!locked) {
                log.warn("Trade skipped, lock unavailable, orderNo={}", order.getOrderNo());
                return;
            }

            BigDecimal currentPrice = getCurrentStockPrice(order.getSymbol());
            if (currentPrice == null) {
                hybridOrderBook.addLimitOrder(order);
                log.warn("Trade postponed, market price unavailable, symbol={}, orderNo={}",
                        order.getSymbol(), order.getOrderNo());
                return;
            }

            TradeDirectionStrategy strategy = directionRouter.route(order.getDirection());
            if (strategy.canExecute(order, currentPrice)) {
                tradeDealExecutor.execute(order, currentPrice, order.getQuantity());
                return;
            }

            hybridOrderBook.addLimitOrder(order);
            log.info("Limit order queued, symbol={}, orderNo={}, orderPrice={}, currentPrice={}",
                    order.getSymbol(), order.getOrderNo(), order.getPrice(), currentPrice);
        } finally {
            if (locked) {
                redissonLockUtil.unlock(lockKey);
            }
        }
    }

    private BigDecimal getCurrentStockPrice(String symbol) {
        try {
            String market = symbol.startsWith("60") ? "sh" : "sz";
            Map<String, Object> realTimeQuote = realTimeStockService.getRealTimeQuote(market, symbol);
            BigDecimal price = (BigDecimal) realTimeQuote.get("price");
            if (price != null) {
                return price;
            }
            log.warn("Market service returned no price, using default price, symbol={}", symbol);
            return new BigDecimal("10.00");
        } catch (Exception e) {
            log.error("Failed to fetch market price, symbol={}", symbol, e);
            return null;
        }
    }
}
