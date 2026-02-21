package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.enums.ErrorEnum;
import com.lghj.exception.BusinessException;
import com.lghj.mapper.SimAccountMapper;
import com.lghj.mapper.TradeDealMapper;
import com.lghj.mapper.TradeOrderMapper;
import com.lghj.mapper.UserPositionMapper;
import com.lghj.pojo.entity.SimAccount;
import com.lghj.pojo.entity.TradeDeal;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.pojo.entity.UserPosition;
import com.lghj.service.IRealTimeStockService;
import com.lghj.service.ITradeService;
import com.lghj.utils.OrderBook;
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
public class TradeServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements ITradeService, OrderQueueManager.OrderProcessor {

    private final TradeDealMapper tradeDealMapper;
    private final SimAccountMapper simAccountMapper;
    private final UserPositionMapper userPositionMapper;
    private final OrderQueueManager orderQueueManager;
    private final RedissonLockUtil redissonLockUtil;
    private final OrderBook orderBook;
    private final IRealTimeStockService realTimeStockService;

    @PostConstruct
    public void init() {
        orderQueueManager.setOrderProcessor(this);
        orderBook.setOrderProcessor(this);
        log.info("TradeService初始化完成，已设置OrderQueueManager和OrderBook的处理器");
    }

    /**
     * 创建委托单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(Long userId, String symbol, Short direction, double price, int quantity) {

        // 生成委托单号
        String orderNo = "ORDER" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 使用乐观锁获取账户信息，防止并发资金超扣
        SimAccount account = simAccountMapper.selectById(userId);

        // 计算总金额 (价格 * 数量 * 100)，假设 quantity 是手
        BigDecimal totalAmount = new BigDecimal(price).multiply(new BigDecimal(quantity).multiply(new BigDecimal(100)));

        // 预检查：检查账户资金或持仓是否充足
        if (direction == 1) { // 买入
            if (account == null) {
                throw new BusinessException(ErrorEnum.ACCOUNT_NOT_FOUND, "用户账户不存在");
            }
            if (account.getAvailableCash().compareTo(totalAmount) < 0) {
                throw new BusinessException(ErrorEnum.DONT_HAVE_ENOUGH_MONEY, "账户可用资金不足");
            }
            // 冻结资金
            account.setFrozenCash(account.getFrozenCash().add(totalAmount)); // 加上本次涉及的金额
            account.setAvailableCash(account.getAvailableCash().subtract(totalAmount)); // 减去本次涉及的金额

        } else if (direction == 2) { // 卖出
            UserPosition position = getUserPosition(userId, symbol);
            if (position == null || position.getAvailableQuantity() < quantity) {
                throw new BusinessException(ErrorEnum.POSITION_NOT_ENOUGH, "持仓不足");
            }
            // 冻结持仓股
            position.setFrozenQuantity(position.getFrozenQuantity() + quantity);
            position.setAvailableQuantity(position.getTotalQuantity() - quantity);

            // 使用MyBatis-Plus的乐观锁机制更新持仓
            int updateResult = userPositionMapper.updateById(position);
            if (updateResult == 0) {
                log.warn("更新持仓失败，可能存在并发操作，用户ID：{}，股票代码：{}", userId, symbol);
                throw new BusinessException(ErrorEnum.POSITION_UPDATE_FAIL, "更新持仓失败，可能存在并发操作");
            }
        }

        // 创建委托单
        TradeOrder order = TradeOrder.builder()
                .orderNo(orderNo)
                .userId(userId)
                .symbol(symbol)
                .direction(direction)
                .price(new BigDecimal(price))
                .quantity(quantity)
                .tradedQuantity(0)
                .status((short) 1) // 1-待定
                .build();

        // 保存委托单
        // 先持久化订单到数据库，确保数据安全
        save(order);
        log.info("委托单已持久化到数据库，委托单号：{}，用户ID：{}，股票代码：{}，方向：{}，价格：{}，数量：{}",
                orderNo, userId, symbol, direction, price, quantity);

        // 使用MyBatis-Plus的乐观锁机制更新账户
        int updateResult = simAccountMapper.updateById(account);
        if (updateResult == 0) {
            log.warn("更新账户失败，可能存在并发操作，用户ID：{}", order.getUserId());
            throw new BusinessException(ErrorEnum.ACCOUNT_UPDATE_FAIL, "更新账户失败，可能存在并发操作");
        }

//        // 处理交易逻辑
//        processTrade(order);
        // 将订单加入内存队列，由撮合引擎处理
        orderQueueManager.addOrder(order);

        return order;
    }

    /**
     * 进入撮合引擎
     *
     * @param order
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(TradeOrder order) {
        // 实现OrderProcessor接口，处理队列中的订单
        try {
            log.info("撮合引擎开始处理订单，委托单号：{}，股票代码：{}", order.getOrderNo(), order.getSymbol());
            // 处理交易逻辑
            processTrade(order);
            log.info("撮合引擎处理订单完成，委托单号：{}，股票代码：{}", order.getOrderNo(), order.getSymbol());
        } catch (Exception e) {
            log.error("撮合引擎处理订单失败，委托单号：{}，股票代码：{}", order.getOrderNo(), order.getSymbol(), e);
        }
    }

    /**
     * 撤销委托单 订单撤销：当多个实例同时尝试撤销同一个订单时，分布式锁确保只有一个实例能够执行撤销操作
     *
     * @param orderId 委托单ID
     * @param userId  用户ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, Long userId) {

        // 查找委托单
        TradeOrder order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            log.warn("委托单不存在或无权限撤销，委托单ID：{}，用户ID：{}", orderId, userId);
            return false;
        }

        // 检查委托单状态
        if (order.getStatus() == 3) { // 3-已完成
            log.warn("委托单已完成，无法撤销，委托单ID：{}", orderId);
            return false;
        }

        String lockKey = RedissonLockUtil.generateTradeLockKey(userId, order.getSymbol());
        boolean locked = false;

        try {
            // 尝试获取分布式锁，最多等待5秒，持有30秒
            locked = redissonLockUtil.tryLock(lockKey, 5, 30);
            if (!locked) {
                log.warn("获取分布式锁失败，无法撤销订单，委托单ID：{}", orderId);
                return false;
            }

            // 更新委托单状态为已撤销
            order.setStatus((short) 4); // 4-已撤销
            order.setCancelTime(LocalDateTime.now());
            updateById(order);

            // 从订单簿中移除已撤销的订单
            orderBook.removeOrder(order);

            // 处理资金和持仓的解冻
            if (order.getDirection() == 1) { // 买入
                // 解冻资金
                SimAccount account = simAccountMapper.selectById(userId);
                if (account != null) {
                    BigDecimal frozenAmount = order.getPrice().multiply(new BigDecimal(order.getQuantity() * 100));
                    account.setFrozenCash(account.getFrozenCash().subtract(frozenAmount));
                    account.setAvailableCash(account.getAvailableCash().add(frozenAmount));

                    // 使用MyBatis-Plus的乐观锁机制更新账户
                    int updateResult = simAccountMapper.updateById(account);
                    if (updateResult == 0) {
                        log.warn("更新账户失败，可能存在并发操作，用户ID：{}", userId);
                        throw new BusinessException(ErrorEnum.ACCOUNT_UPDATE_FAIL, "更新账户失败，可能存在并发操作");
                    }
                }
            } else if (order.getDirection() == 2) { // 卖出
                // 解冻持仓
                UserPosition position = getUserPosition(userId, order.getSymbol());
                if (position != null) {
                    position.setFrozenQuantity(position.getFrozenQuantity() - order.getQuantity());
                    position.setAvailableQuantity(position.getAvailableQuantity() + order.getQuantity());

                    // 使用MyBatis-Plus的乐观锁机制更新持仓
                    int updateResult = userPositionMapper.updateById(position);
                    if (updateResult == 0) {
                        log.warn("更新持仓失败，可能存在并发操作，用户ID：{}，股票代码：{}", userId, order.getSymbol());
                        throw new BusinessException(ErrorEnum.POSITION_UPDATE_FAIL, "更新持仓失败，可能存在并发操作");
                    }
                }
            }

            log.info("撤销委托单，委托单ID：{}，用户ID：{}", orderId, userId);
            return true;
        } finally {
            // 释放分布式锁
            if (locked) {
                redissonLockUtil.unlock(lockKey);
            }
        }
    }

    /**
     * 获取用户的委托单列表
     *
     * @param userId 用户ID
     * @return 委托单列表
     */
    @Override
    public List<TradeOrder> getUserOrders(Long userId) {
        LambdaQueryWrapper<TradeOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeOrder::getUserId, userId)
                .eq(TradeOrder::getIsDeleted, 0)
                .orderByDesc(TradeOrder::getCreateTime);
        return list(queryWrapper);
    }

    /**
     * 获取交易记录
     *
     * @param userId 用户ID
     * @return
     */
    @Override
    public List<TradeDeal> getUserDeals(Long userId) {
        LambdaQueryWrapper<TradeDeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TradeDeal::getUserId, userId)
                .eq(TradeDeal::getIsDeleted, 0)
                .orderByDesc(TradeDeal::getCreateTime);
        return tradeDealMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询交易记录
     *
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param symbol
     * @return
     */
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

    /**
     * 处理交易逻辑 交易处理：当多个实例同时处理同一个用户的同一只股票的交易时，分布式锁确保只有一个实例能够执行交易逻辑
     *
     * @param order 委托单
     */
    private void processTrade(TradeOrder order) {

        // 使用Redisson实现分布式锁
        String lockKey = RedissonLockUtil.generateTradeLockKey(order.getUserId(), order.getSymbol());
        boolean locked = false;

        try {
            // 尝试获取分布式锁，最多等待5秒，持有30秒
            locked = redissonLockUtil.tryLock(lockKey, 5, 30);
            if (!locked) {
                log.warn("获取分布式锁失败，无法处理交易，委托单号：{}", order.getOrderNo());
                return;
            }

            // 获取实时股票价格
            BigDecimal currentPrice = getCurrentStockPrice(order.getSymbol());
            if (currentPrice == null) {
                log.warn("获取实时股票价格失败，无法处理交易，股票代码：{}", order.getSymbol());
                // 将订单添加到订单簿，等待后续行情更新
                orderBook.addLimitOrder(order);
                return;
            }

            log.info("处理交易，股票代码：{}，当前价格：{}，委托价格：{}",
                    order.getSymbol(), currentPrice, order.getPrice());

            // 根据实时价格判断是否成交
            boolean canExecute = canExecuteOrder(order, currentPrice);
            if (canExecute) {
                // 可以成交，执行交易
                // 注意：以当前市场价格成交，而不是委托价格
                if (order.getDirection() == 1) { // 买入
                    processBuy(order, currentPrice);
                } else if (order.getDirection() == 2) { // 卖出
                    processSell(order, currentPrice);
                }
            } else {
                // 无法立即成交，将限价单添加到订单簿
                orderBook.addLimitOrder(order);
                log.info("限价单无法立即成交，已添加到订单簿，股票代码：{}，委托单号：{}",
                        order.getSymbol(), order.getOrderNo());
            }
        } finally {
            // 释放分布式锁
            if (locked) {
                redissonLockUtil.unlock(lockKey);
            }
        }
    }

    /**
     * 获取实时股票价格
     *
     * @param symbol 股票代码
     * @return 实时价格
     */
    private BigDecimal getCurrentStockPrice(String symbol) {
        try {

            // 从行情服务获取实时价格
            String market = "sz";
            if (symbol.startsWith("60")) {
                market = "sh";
            }
            Map<String, Object> realTimeQuote = realTimeStockService.getRealTimeQuote(market, symbol);

            BigDecimal price = (BigDecimal) realTimeQuote.get("price");
            if (price != null) {
                return price;
            }

            // 如果行情服务没有价格，返回一个默认价格
            log.warn("行情服务没有股票价格，使用默认价格，股票代码：{}", symbol);
            return new BigDecimal("10.00");
        } catch (Exception e) {
            log.error("获取实时股票价格失败，股票代码：{}", symbol, e);
            return null;
        }
    }

    /**
     * 判断订单是否可以成交
     *
     * @param order        订单
     * @param currentPrice 当前价格
     * @return 是否可以成交
     */
    private boolean canExecuteOrder(TradeOrder order, BigDecimal currentPrice) {
        if (order.getDirection() == 1) { // 买入
            // 买单：委托价格 >= 当前价格时可以成交
            return order.getPrice().compareTo(currentPrice) >= 0;
        } else if (order.getDirection() == 2) { // 卖出
            // 卖单：委托价格 <= 当前价格时可以成交
            return order.getPrice().compareTo(currentPrice) <= 0;
        }
        return false;
    }

    /**
     * 处理买入交易
     *
     * @param order     委托单
     * @param dealPrice 成交价格
     */
    private void processBuy(TradeOrder order, BigDecimal dealPrice) {
        // 使用乐观锁获取账户信息，防止并发资金超扣
        SimAccount account = simAccountMapper.selectById(order.getUserId());
        if (account == null) {
            log.warn("用户账户不存在，用户ID：{}", order.getUserId());
            return;
        }

        // 模拟成交（在实际系统中，这里会有撮合逻辑）
        // 使用实时价格作为成交价
        executeDeal(order, dealPrice, order.getQuantity());
    }

    /**
     * 处理卖出交易
     *
     * @param order     委托单
     * @param dealPrice 成交价格
     */
    private void processSell(TradeOrder order, BigDecimal dealPrice) {
        // 使用乐观锁获取持仓信息，防止并发卖出导致的超卖
        UserPosition position = getUserPosition(order.getUserId(), order.getSymbol());

        if (position == null || position.getAvailableQuantity() < order.getQuantity()) {
            log.warn("持仓不足，用户ID：{}，股票代码：{}，可用数量：{}，卖出数量：{}",
                    order.getUserId(), order.getSymbol(),
                    position != null ? position.getAvailableQuantity() : 0, order.getQuantity());
            return;
        }

        // 冻结持仓
        position.setFrozenQuantity(position.getFrozenQuantity() + order.getQuantity());
        position.setAvailableQuantity(position.getAvailableQuantity() - order.getQuantity());

        // 使用MyBatis-Plus的乐观锁机制更新持仓
        int updateResult = userPositionMapper.updateById(position);
        if (updateResult == 0) {
            log.warn("更新持仓失败，可能存在并发操作，用户ID：{}，股票代码：{}", order.getUserId(), order.getSymbol());
            throw new BusinessException(ErrorEnum.POSITION_UPDATE_FAIL, "更新持仓失败，可能存在并发操作");
        }

        // 模拟成交
        // 使用实时价格作为成交价
        executeDeal(order, dealPrice, order.getQuantity());
    }

    /**
     * 执行成交
     *
     * @param order        委托单
     * @param dealPrice    成交价格
     * @param dealQuantity 成交数量
     */
    private void executeDeal(TradeOrder order, BigDecimal dealPrice, int dealQuantity) {
        // 生成成交单号
        String dealNo = "DEAL" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 创建成交记录
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

        // 更新委托单
        order.setTradedQuantity(order.getTradedQuantity() + dealQuantity);
        if (order.getTradedQuantity() >= order.getQuantity()) {
            order.setStatus((short) 3); // 3-已完成
            // 订单完成，从订单簿中移除
            orderBook.removeOrder(order);
        } else {
            order.setStatus((short) 2); // 2-部分完成
        }
        updateById(order);

        // 处理资金和持仓
        if (order.getDirection() == 1) { // 买入
            processBuyDeal(order, dealPrice, dealQuantity);
        } else if (order.getDirection() == 2) { // 卖出
            processSellDeal(order, dealPrice, dealQuantity);
        }

        log.info("执行成交，成交单号：{}，委托单号：{}，股票代码：{}，价格：{}，数量：{}",
                dealNo, order.getOrderNo(), order.getSymbol(), dealPrice, dealQuantity);
    }

    /**
     * 处理买入成交
     *
     * @param order    委托单
     * @param price    成交价格
     * @param quantity 成交数量
     */
    private void processBuyDeal(TradeOrder order, BigDecimal price, int quantity) {
        Long userId = order.getUserId();
        String symbol = order.getSymbol();

        // 扣减资金
        SimAccount account = simAccountMapper.selectById(userId);
        if (account != null) {
            // 实际成交金额 = 成交价 * 数量 * 100
            BigDecimal actualAmount = price.multiply(new BigDecimal(quantity * 100));
            // 原冻结金额 = 委托价 * 数量 * 100
            BigDecimal frozenAmount = order.getPrice().multiply(new BigDecimal(quantity * 100));

            // 更新资金：
            // 1. 扣除实际花费 (TotalCash)
            // 2. 解冻原冻结金额 (FrozenCash)
            // 3. 将差额退还到可用资金 (AvailableCash = AvailableCash + (Frozen - Actual))
            account.setTotalCash(account.getTotalCash().subtract(actualAmount));
            account.setFrozenCash(account.getFrozenCash().subtract(frozenAmount));
            account.setAvailableCash(account.getAvailableCash().add(frozenAmount.subtract(actualAmount)));

            // 使用MyBatis-Plus的乐观锁机制更新账户
            int updateResult = simAccountMapper.updateById(account);
            if (updateResult == 0) {
                log.warn("更新账户失败，可能存在并发操作，用户ID：{}", userId);
                throw new BusinessException(ErrorEnum.ACCOUNT_UPDATE_FAIL, "更新账户失败，可能存在并发操作");
            }
        }

        // 更新持仓
        UserPosition position = getUserPosition(userId, symbol);
        if (position == null) {
            // 新建持仓
            position = UserPosition.builder()
                    .userId(userId)
                    .accountId(userId) // 简化处理，使用用户ID作为账户ID
                    .symbol(symbol)
                    .totalQuantity(quantity)
                    .frozenQuantity(0)
                    .availableQuantity(quantity)
                    .costPrice(price)
                    .profitLoss(BigDecimal.ZERO)
                    .version(1) // 初始版本号
                    .build();
            userPositionMapper.insert(position);
        } else {
            // 更新现有持仓
            int newTotalQuantity = position.getTotalQuantity() + quantity; // 加上新买的持仓数
            BigDecimal newCostPrice = position.getCostPrice().multiply(new BigDecimal(position.getTotalQuantity()))
                    .add(price.multiply(new BigDecimal(quantity)))
                    .divide(new BigDecimal(newTotalQuantity), 2, BigDecimal.ROUND_HALF_UP); // 计算新的平均成本价

            position.setTotalQuantity(newTotalQuantity);
            position.setAvailableQuantity(position.getAvailableQuantity() + quantity);
            position.setCostPrice(newCostPrice);

            // 使用MyBatis-Plus的乐观锁机制更新持仓
            int updateResult = userPositionMapper.updateById(position);
            if (updateResult == 0) {
                log.warn("更新持仓失败，可能存在并发操作，用户ID：{}，股票代码：{}", userId, symbol);
                throw new BusinessException(ErrorEnum.POSITION_UPDATE_FAIL, "更新持仓失败，可能存在并发操作");
            }
        }
    }

    /**
     * 处理卖出成交
     *
     * @param order    委托单
     * @param price    成交价格
     * @param quantity 成交数量
     */
    private void processSellDeal(TradeOrder order, BigDecimal price, int quantity) {
        Long userId = order.getUserId();
        String symbol = order.getSymbol();

        // 增加资金
        SimAccount account = simAccountMapper.selectById(userId);
        if (account != null) {
            // 卖出总金额 = 成交价 * 数量 * 100
            BigDecimal totalAmount = price.multiply(new BigDecimal(quantity).multiply(new BigDecimal(100)));
            account.setAvailableCash(account.getAvailableCash().add(totalAmount));
            account.setTotalCash(account.getTotalCash().add(totalAmount));
            // 使用MyBatis-Plus的乐观锁机制更新账户
            int updateResult = simAccountMapper.updateById(account);
            if (updateResult == 0) {
                log.warn("更新账户失败，可能存在并发操作，用户ID：{}", userId);
                throw new BusinessException(ErrorEnum.ACCOUNT_UPDATE_FAIL, "更新账户失败，可能存在并发操作");
            }
        }

        // 更新持仓
        UserPosition position = getUserPosition(userId, symbol);
        if (position != null) {
            int newTotalQuantity = position.getTotalQuantity() - quantity;
            if (newTotalQuantity <= 0) {
                // 清空持仓
                userPositionMapper.deleteById(position.getId());
            } else {
                // 更新持仓
                position.setTotalQuantity(newTotalQuantity);
                position.setFrozenQuantity(position.getFrozenQuantity() - quantity);

                // 使用MyBatis-Plus的乐观锁机制更新持仓
                int updateResult = userPositionMapper.updateById(position);
                if (updateResult == 0) {
                    log.warn("更新持仓失败，可能存在并发操作，用户ID：{}，股票代码：{}", userId, symbol);
                    throw new BusinessException(ErrorEnum.POSITION_UPDATE_FAIL, "更新持仓失败，可能存在并发操作");
                }
            }
        }
    }

    /**
     * 获取用户对特定股票的持仓
     *
     * @param userId 用户ID
     * @param symbol 股票代码
     * @return 持仓信息
     */
    private UserPosition getUserPosition(Long userId, String symbol) {
        LambdaQueryWrapper<UserPosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPosition::getUserId, userId)
                .eq(UserPosition::getSymbol, symbol)
                .eq(UserPosition::getIsDeleted, 0);
        return userPositionMapper.selectOne(queryWrapper);
    }
}
