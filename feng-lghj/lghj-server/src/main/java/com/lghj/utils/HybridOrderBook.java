package com.lghj.utils;

import com.alibaba.fastjson.JSON;
import com.lghj.pojo.entity.TradeOrder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 混合订单簿 - 冷热数据分层存储的撮合引擎
 *
 * <p>
 * 设计背景： 传统订单簿将所有订单存储在内存中，当订单数量过多时会导致内存压力过大甚至OOM。
 * 本方案采用冷热数据分层存储策略，将价格偏离当前行情较远的订单存入Redis， 热数据保留在内存优先队列中，实现内存占用可控的同时保持纳秒级撮合性能。
 *
 * <p>
 * 核心机制：
 * <ul>
 * <li>热数据判定：价格在当前价格 ±5% 范围内的订单视为热数据</li>
 * <li>溢出降级：热队列超过100条时，将低优先级订单降级到Redis</li>
 * <li>价格晋升：当前价格变化时，将符合条件的冷数据晋升到热队列</li>
 * <li>快速查询：Redis Sorted Set 按价格排序，O(logN) 复杂度</li>
 * </ul>
 *
 * <p>
 * 数据结构：
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │              热数据层（内存 PriorityBlockingQueue）          │
 * │   • 存储价格在阈值范围内的订单                              │
 * │   • 最大容量 100 条                                         │
 * │   • 撮合速度极快（纳秒级）                                   │
 * │                                                             │
 * │   买单队列（价格高→低）      卖单队列（价格低→高）           │
 * │   [10.50, 10.40, 10.30]     [9.50, 9.60, 9.70]             │
 * └─────────────────────────────────────────────────────────────┘
 *                            ↑↓ 晋升/降级
 * ┌─────────────────────────────────────────────────────────────┐
 * │              冷数据层（Redis Sorted Set）                    │
 * │   • 存储价格偏离阈值较远的订单                              │
 * │   • 无容量限制                                              │
 * │   • 持久化存储，支持快速范围查询                             │
 * │                                                             │
 * │   买单（价格>10.50）         卖单（价格<9.50）              │
 * │   [12.0, 11.5, 11.0]        [8.0, 8.5, 9.0]                │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * @author lghj
 * @see OrderQueueManager
 */
@Slf4j
@Component
public class HybridOrderBook {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 订单处理器回调接口 当订单满足成交条件时，通过此接口回调 TradeServiceImpl 处理成交逻辑
     */
    @Setter
    private OrderQueueManager.OrderProcessor orderProcessor;

    /**
     * 股票订单簿映射表 Key: 股票代码（如 "sh600000"） Value: 该股票的混合订单簿实例
     *
     * 使用 ConcurrentHashMap 保证线程安全，支持并发读写
     */
    private final Map<String, HybridStockOrderBook> stockOrderBooks = new ConcurrentHashMap<>();

    /**
     * 热数据队列最大容量 超过此容量时触发溢出降级机制，将低优先级订单移至Redis
     */
    private static final int HOT_QUEUE_MAX_SIZE = 100;

    /**
     * 热数据价格偏差率 当前价格 ±5% 范围内的订单视为热数据 例如：当前价格10元，则9.5-10.5元范围内的订单为热数据
     */
    private static final BigDecimal PRICE_DEVIATION_RATE = new BigDecimal("0.05");

    /**
     * Redis Key 前缀 完整Key格式：orderbook:{symbol}:buy 或 orderbook:{symbol}:sell
     */
    private static final String REDIS_KEY_PREFIX = "orderbook:";

    /**
     * Redis 买单Key后缀
     */
    private static final String REDIS_BUY_KEY_SUFFIX = ":buy";

    /**
     * Redis 卖单Key后缀
     */
    private static final String REDIS_SELL_KEY_SUFFIX = ":sell";

    /**
     * 构造函数
     *
     * @param stringRedisTemplate Redis操作模板，用于冷数据存储
     */
    public HybridOrderBook(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 获取所有活跃股票代码
     *
     * @return 活跃股票代码集合
     */
    public Set<String> getActiveSymbols() {
        return stockOrderBooks.keySet();
    }

    /**
     * 获取或创建指定股票的订单簿 使用 computeIfAbsent 保证线程安全的懒加载
     *
     * @param symbol 股票代码
     * @return 该股票的混合订单簿实例
     */
    public HybridStockOrderBook getStockOrderBook(String symbol) {
        return stockOrderBooks.computeIfAbsent(symbol, k -> new HybridStockOrderBook(symbol));
    }

    /**
     * 添加限价单到订单簿
     *
     * <p>
     * 处理流程：
     * <ol>
     * <li>获取或创建该股票的订单簿</li>
     * <li>判断订单价格是否在热数据区间</li>
     * <li>热数据加入内存队列，冷数据加入Redis</li>
     * <li>如果内存队列溢出，触发降级机制</li>
     * </ol>
     *
     * @param order 委托单
     */
    public void addLimitOrder(TradeOrder order) {
        HybridStockOrderBook orderBook = getStockOrderBook(order.getSymbol());
        orderBook.addOrder(order);
        log.info("限价单已添加到混合订单簿，股票代码：{}，委托单号：{}，价格：{}，数量：{}",
                order.getSymbol(), order.getOrderNo(), order.getPrice(), order.getQuantity());
    }

    /**
     * 从订单簿中移除订单
     *
     * <p>
     * 移除场景：
     * <ul>
     * <li>用户撤销委托单</li>
     * <li>订单全部成交</li>
     * </ul>
     *
     * <p>
     * 处理流程：
     * <ol>
     * <li>从订单索引中移除</li>
     * <li>从内存队列中移除（如果存在）</li>
     * <li>从Redis中移除（如果存在）</li>
     * </ol>
     *
     * @param order 要移除的委托单
     */
    public void removeOrder(TradeOrder order) {
        HybridStockOrderBook orderBook = stockOrderBooks.get(order.getSymbol());
        if (orderBook != null) {
            orderBook.removeOrder(order);
            log.info("订单已从混合订单簿中移除，股票代码：{}，委托单号：{}", order.getSymbol(), order.getOrderNo());
        }
    }

    /**
     * 处理行情数据，触发撮合
     *
     * <p>
     * 由 MarketDataSchedulerTask 定时调用（每3秒）
     *
     * <p>
     * 处理流程：
     * <ol>
     * <li>更新热数据价格区间</li>
     * <li>从Redis晋升符合条件的订单到内存</li>
     * <li>遍历内存队列，找出可成交订单</li>
     * <li>回调 orderProcessor 执行成交</li>
     * </ol>
     *
     * @param symbol 股票代码
     * @param currentPrice 当前价格
     */
    public void processMarketData(String symbol, BigDecimal currentPrice) {
        HybridStockOrderBook orderBook = stockOrderBooks.get(symbol);
        if (orderBook == null) {
            return;
        }
        orderBook.processMarketData(currentPrice, this.orderProcessor);
    }

    /**
     * 单只股票的混合订单簿
     *
     * <p>
     * 包含该股票的所有待成交订单，分为热数据和冷数据两层存储
     */
    public class HybridStockOrderBook {

        /**
         * 股票代码
         */
        private final String symbol;

        /**
         * 最新成交价格（用于计算热数据区间）
         */
        private volatile BigDecimal lastPrice = BigDecimal.ZERO;

        /**
         * 热数据价格区间下限 = lastPrice * (1 - 0.05)
         */
        private volatile BigDecimal hotPriceLower = BigDecimal.ZERO;

        /**
         * 热数据价格区间上限 = lastPrice * (1 + 0.05)
         */
        private volatile BigDecimal hotPriceUpper = BigDecimal.ZERO;

        /**
         * 热数据买单队列
         *
         * <p>
         * 排序规则：价格从高到低（价格高的买单优先撮合）
         * <p>
         * 原因：买单愿意以更高价格买入，当价格下跌时更容易成交
         *
         * <p>
         * 示例：当前价格10元，队列中有 [10.5, 10.3, 10.1] 当价格跌到10.5以下时，10.5的买单最先成交
         */
        private final PriorityBlockingQueue<TradeOrder> hotBuyOrders = new PriorityBlockingQueue<>(
                HOT_QUEUE_MAX_SIZE, (o1, o2) -> o2.getPrice().compareTo(o1.getPrice()));

        /**
         * 热数据卖单队列
         *
         * <p>
         * 排序规则：价格从低到高（价格低的卖单优先撮合）
         * <p>
         * 原因：卖单愿意以更低价格卖出，当价格上涨时更容易成交
         *
         * <p>
         * 示例：当前价格10元，队列中有 [9.5, 9.7, 9.9] 当价格涨到9.5以上时，9.5的卖单最先成交
         */
        private final PriorityBlockingQueue<TradeOrder> hotSellOrders = new PriorityBlockingQueue<>(
                HOT_QUEUE_MAX_SIZE, Comparator.comparing(TradeOrder::getPrice));

        /**
         * 订单索引
         *
         * <p>
         * Key: 订单ID
         * <p>
         * Value: 订单对象
         *
         * <p>
         * 作用：
         * <ul>
         * <li>快速判断订单是否存在于订单簿</li>
         * <li>支持订单撤销时的快速定位</li>
         * </ul>
         */
        private final Map<Long, TradeOrder> orderIndex = new ConcurrentHashMap<>();

        /**
         * 构造函数
         *
         * @param symbol 股票代码
         */
        public HybridStockOrderBook(String symbol) {
            this.symbol = symbol;
        }

        /**
         * 添加订单到订单簿
         *
         * <p>
         * 处理逻辑：
         * <pre>
         * 1. 将订单加入索引（用于快速查找和去重）
         * 2. 判断订单价格是否在热数据区间：
         *    - 如果 lastPrice > 0 且价格不在热数据区间 → 加入Redis冷存储
         *    - 否则 → 加入内存热队列
         * 3. 如果热队列溢出 → 触发降级机制
         * </pre>
         *
         * @param order 委托单
         */
        public void addOrder(TradeOrder order) {
            // 加入订单索引
            orderIndex.put(order.getId(), order);

            // 判断是否应该存入冷数据
            // 条件：已有价格基准 且 价格偏离热数据区间
            if (lastPrice.compareTo(BigDecimal.ZERO) > 0 && !isInHotRange(order.getPrice())) {
                addToColdQueue(order);
            } else {
                // 检查热队列是否溢出
                if (hotBuyOrders.size() >= HOT_QUEUE_MAX_SIZE || hotSellOrders.size() >= HOT_QUEUE_MAX_SIZE) {
                    overflowToColdStorage();
                }
                // 加入热队列
                if (order.getDirection() == 1) {
                    hotBuyOrders.offer(order);
                } else if (order.getDirection() == 2) {
                    hotSellOrders.offer(order);
                }
            }
        }

        /**
         * 从订单簿中移除订单
         *
         * <p>
         * 移除操作：
         * <ul>
         * <li>从订单索引中移除</li>
         * <li>从热数据队列中移除（PriorityBlockingQueue的remove是O(n)操作）</li>
         * <li>从Redis冷存储中移除</li>
         * </ul>
         *
         * @param order 要移除的委托单
         */
        public void removeOrder(TradeOrder order) {
            // 从索引中移除
            orderIndex.remove(order.getId());

            // 从热队列中移除
            if (order.getDirection() == 1) {
                hotBuyOrders.remove(order);
                removeFromColdStorage(order, REDIS_BUY_KEY_SUFFIX);
            } else if (order.getDirection() == 2) {
                hotSellOrders.remove(order);
                removeFromColdStorage(order, REDIS_SELL_KEY_SUFFIX);
            }
        }

        /**
         * 处理行情数据，执行撮合逻辑
         *
         * <p>
         * 撮合流程：
         * <pre>
         * 1. 更新热数据价格区间
         *    - 如果价格变化，重新计算 hotPriceLower 和 hotPriceUpper
         *
         * 2. 晋升冷数据
         *    - 从Redis中查询符合新区间的订单
         *    - 将这些订单移到内存热队列
         *
         * 3. 撮合买单
         *    - 遍历热队列，找出委托价 >= 当前价的买单
         *    - 这些买单可以以当前价格成交
         *
         * 4. 撮合卖单
         *    - 遍历热队列，找出委托价 <= 当前价的卖单
         *    - 这些卖单可以以当前价格成交
         *
         * 5. 执行成交
         *    - 回调 orderProcessor 处理每笔可成交订单
         * </pre>
         *
         * @param currentPrice 当前市场价格
         * @param processor 订单处理器回调
         */
        public void processMarketData(BigDecimal currentPrice, OrderQueueManager.OrderProcessor processor) {
            // 更新热数据价格区间，返回是否发生变化
            boolean priceChanged = updateHotPriceRange(currentPrice);

            // 如果价格变化，从冷存储晋升订单到热队列
            if (priceChanged) {
                promoteOrdersFromColdStorage(currentPrice);
            }

            // 收集可成交的买单
            // 条件：委托价格 >= 当前价格（买单愿意以更高价买入，现在价格更低，可以成交）
            List<TradeOrder> executableBuyOrders = new ArrayList<>();
            while (true) {
                TradeOrder buyOrder = hotBuyOrders.peek();
                if (buyOrder == null) {
                    break;
                }
                // 买单委托价 >= 当前价，可以成交
                if (buyOrder.getPrice().compareTo(currentPrice) >= 0) {
                    hotBuyOrders.poll(); // 从队列移除
                    executableBuyOrders.add(buyOrder);
                } else {
                    break; // 后面的订单价格更低，无法成交
                }
            }

            // 收集可成交的卖单
            // 条件：委托价格 <= 当前价格（卖单愿意以更低价卖出，现在价格更高，可以成交）
            List<TradeOrder> executableSellOrders = new ArrayList<>();
            while (true) {
                TradeOrder sellOrder = hotSellOrders.peek();
                if (sellOrder == null) {
                    break;
                }
                // 卖单委托价 <= 当前价，可以成交
                if (sellOrder.getPrice().compareTo(currentPrice) <= 0) {
                    hotSellOrders.poll(); // 从队列移除
                    executableSellOrders.add(sellOrder);
                } else {
                    break; // 后面的订单价格更高，无法成交
                }
            }

            // 执行买单成交
            for (TradeOrder buyOrder : executableBuyOrders) {
                log.info("买单可成交，股票代码：{}，委托单号：{}，委托价格：{}，当前价格：{}",
                        buyOrder.getSymbol(), buyOrder.getOrderNo(), buyOrder.getPrice(), currentPrice);
                orderIndex.remove(buyOrder.getId());
                if (processor != null) {
                    processor.processOrder(buyOrder);
                }
            }

            // 执行卖单成交
            for (TradeOrder sellOrder : executableSellOrders) {
                log.info("卖单可成交，股票代码：{}，委托单号：{}，委托价格：{}，当前价格：{}",
                        sellOrder.getSymbol(), sellOrder.getOrderNo(), sellOrder.getPrice(), currentPrice);
                orderIndex.remove(sellOrder.getId());
                if (processor != null) {
                    processor.processOrder(sellOrder);
                }
            }
        }

        /**
         * 更新热数据价格区间
         *
         * <p>
         * 计算公式：
         * <ul>
         * <li>hotPriceLower = currentPrice * (1 - 0.05)</li>
         * <li>hotPriceUpper = currentPrice * (1 + 0.05)</li>
         * </ul>
         *
         * <p>
         * 示例：当前价格10元
         * <ul>
         * <li>hotPriceLower = 10 * 0.95 = 9.5</li>
         * <li>hotPriceUpper = 10 * 1.05 = 10.5</li>
         * </ul>
         *
         * @param currentPrice 当前价格
         * @return 价格是否发生变化
         */
        private boolean updateHotPriceRange(BigDecimal currentPrice) {
            if (lastPrice.compareTo(currentPrice) == 0) {
                return false; // 价格未变化
            }
            lastPrice = currentPrice;
            // 计算偏差值 = 当前价格 * 5%
            BigDecimal deviation = currentPrice.multiply(PRICE_DEVIATION_RATE).setScale(2, RoundingMode.HALF_UP);
            hotPriceLower = currentPrice.subtract(deviation);
            hotPriceUpper = currentPrice.add(deviation);
            log.debug("更新热数据价格区间，股票代码：{}，当前价格：{}，区间：[{}, {}]",
                    symbol, currentPrice, hotPriceLower, hotPriceUpper);
            return true;
        }

        /**
         * 判断价格是否在热数据区间内
         *
         * @param price 订单价格
         * @return true-在热数据区间，false-在冷数据区间
         */
        private boolean isInHotRange(BigDecimal price) {
            return price.compareTo(hotPriceLower) >= 0 && price.compareTo(hotPriceUpper) <= 0;
        }

        /**
         * 将订单加入Redis冷存储
         *
         * <p>
         * 使用 Redis Sorted Set 存储：
         * <ul>
         * <li>member: 订单JSON序列化字符串</li>
         * <li>score: 订单价格（用于范围查询）</li>
         * </ul>
         *
         * <p>
         * Redis Key 格式：
         * <ul>
         * <li>买单：orderbook:{symbol}:buy</li>
         * <li>卖单：orderbook:{symbol}:sell</li>
         * </ul>
         *
         * @param order 委托单
         */
        private void addToColdQueue(TradeOrder order) {
            String redisKey = getRedisKey(order.getDirection());
            double score = order.getPrice().doubleValue();
            String orderJson = serializeOrder(order);
            stringRedisTemplate.opsForZSet().add(redisKey, orderJson, score);
            log.debug("订单加入冷数据存储，股票代码：{}，委托单号：{}，价格：{}",
                    order.getSymbol(), order.getOrderNo(), order.getPrice());
        }

        /**
         * 从Redis冷存储中移除订单
         *
         * @param order 要移除的委托单
         * @param suffix Redis Key后缀（:buy 或 :sell）
         */
        private void removeFromColdStorage(TradeOrder order, String suffix) {
            String redisKey = REDIS_KEY_PREFIX + symbol + suffix;
            String orderJson = serializeOrder(order);
            stringRedisTemplate.opsForZSet().remove(redisKey, orderJson);
        }

        /**
         * 热队列溢出降级到冷存储
         *
         * <p>
         * 触发条件：热队列大小超过 HOT_QUEUE_MAX_SIZE（100）
         *
         * <p>
         * 降级策略：
         * <ul>
         * <li>移出队列中优先级最低的一半订单（50个）</li>
         * <li>买单：移出价格最低的（队列尾部）</li>
         * <li>卖单：移出价格最高的（队列尾部）</li>
         * </ul>
         *
         * <p>
         * 注意：由于 PriorityBlockingQueue 只能从头部取出， 实际移出的是队列中优先级较低的订单
         */
        private void overflowToColdStorage() {
            int overflowCount = HOT_QUEUE_MAX_SIZE / 2; // 移出50个

            // 买单溢出：移出价格较低的订单
            for (int i = 0; i < overflowCount && hotBuyOrders.size() > HOT_QUEUE_MAX_SIZE / 2; i++) {
                TradeOrder order = hotBuyOrders.poll();
                if (order != null) {
                    addToColdQueue(order);
                }
            }

            // 卖单溢出：移出价格较高的订单
            for (int i = 0; i < overflowCount && hotSellOrders.size() > HOT_QUEUE_MAX_SIZE / 2; i++) {
                TradeOrder order = hotSellOrders.poll();
                if (order != null) {
                    addToColdQueue(order);
                }
            }
        }

        /**
         * 从冷存储晋升订单到热队列
         *
         * <p>
         * 触发时机：当前价格变化导致热数据区间变化
         *
         * <p>
         * 晋升条件：
         * <ul>
         * <li>买单：价格 >= 新的热数据区间下限</li>
         * <li>卖单：价格 <= 新的热数据区间上限</li>
         * <
         * /ul>
         *
         * @param currentPrice 当前价格
         */
        private void promoteOrdersFromColdStorage(BigDecimal currentPrice) {
            promoteBuyOrders(currentPrice);
            promoteSellOrders(currentPrice);
        }

        /**
         * 晋升买单
         *
         * <p>
         * 查询 Redis Sorted Set 中 score >= hotPriceLower 的订单 这些订单的价格现在落入了热数据区间
         *
         * @param currentPrice 当前价格（未使用，保留用于扩展）
         */
        private void promoteBuyOrders(BigDecimal currentPrice) {
            String redisKey = REDIS_KEY_PREFIX + symbol + REDIS_BUY_KEY_SUFFIX;

            // 查询价格 >= hotPriceLower 的所有订单
            Set<String> ordersToPromote = stringRedisTemplate.opsForZSet()
                    .rangeByScore(redisKey, hotPriceLower.doubleValue(), Double.MAX_VALUE);

            if (ordersToPromote == null || ordersToPromote.isEmpty()) {
                return;
            }

            for (String orderJson : ordersToPromote) {
                TradeOrder order = deserializeOrder(orderJson);
                if (order != null && isInHotRange(order.getPrice())) {
                    hotBuyOrders.offer(order);
                    stringRedisTemplate.opsForZSet().remove(redisKey, orderJson);
                    log.debug("买单从冷存储晋升到热队列，股票代码：{}，委托单号：{}，价格：{}",
                            order.getSymbol(), order.getOrderNo(), order.getPrice());
                }
            }
        }

        /**
         * 晋升卖单
         *
         * <p>
         * 查询 Redis Sorted Set 中 score <= hotPriceUpper 的订单 这些订单的价格现在落入了热数据区间
         *
         * @param currentPrice 当前价格（未使用，保留用于扩展）
         */
        private void promoteSellOrders(BigDecimal currentPrice) {
            String redisKey = REDIS_KEY_PREFIX + symbol + REDIS_SELL_KEY_SUFFIX;

            // 查询价格 <= hotPriceUpper 的所有订单
            Set<String> ordersToPromote = stringRedisTemplate.opsForZSet()
                    .rangeByScore(redisKey, 0, hotPriceUpper.doubleValue());

            if (ordersToPromote == null || ordersToPromote.isEmpty()) {
                return;
            }

            for (String orderJson : ordersToPromote) {
                TradeOrder order = deserializeOrder(orderJson);
                if (order != null && isInHotRange(order.getPrice())) {
                    hotSellOrders.offer(order);
                    stringRedisTemplate.opsForZSet().remove(redisKey, orderJson);
                    log.debug("卖单从冷存储晋升到热队列，股票代码：{}，委托单号：{}，价格：{}",
                            order.getSymbol(), order.getOrderNo(), order.getPrice());
                }
            }
        }

        /**
         * 获取Redis Key
         *
         * @param direction 交易方向（1-买入，2-卖出）
         * @return Redis Key
         */
        private String getRedisKey(short direction) {
            return REDIS_KEY_PREFIX + symbol + (direction == 1 ? REDIS_BUY_KEY_SUFFIX : REDIS_SELL_KEY_SUFFIX);
        }

        /**
         * 序列化订单为JSON字符串
         *
         * <p>
         * 序列化字段：
         * <ul>
         * <li>id: 订单ID</li>
         * <li>orderNo: 订单号</li>
         * <li>userId: 用户ID</li>
         * <li>symbol: 股票代码</li>
         * <li>direction: 交易方向</li>
         * <li>price: 委托价格</li>
         * <li>quantity: 委托数量（手）</li>
         * <li>tradedQuantity: 已成交数量</li>
         * <li>status: 订单状态</li>
         * </ul>
         *
         * @param order 委托单
         * @return JSON字符串
         */
        private String serializeOrder(TradeOrder order) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("userId", order.getUserId());
            map.put("symbol", order.getSymbol());
            map.put("direction", order.getDirection());
            map.put("price", order.getPrice().toString());
            map.put("quantity", order.getQuantity());
            map.put("tradedQuantity", order.getTradedQuantity());
            map.put("status", order.getStatus());
            return JSON.toJSONString(map);
        }

        /**
         * 反序列化JSON字符串为订单对象
         *
         * @param json JSON字符串
         * @return 委托单对象，反序列化失败返回null
         */
        private TradeOrder deserializeOrder(String json) {
            try {
                Map<String, Object> map = JSON.parseObject(json, Map.class);
                return TradeOrder.builder()
                        .id(Long.valueOf(map.get("id").toString()))
                        .orderNo(map.get("orderNo").toString())
                        .userId(Long.valueOf(map.get("userId").toString()))
                        .symbol(map.get("symbol").toString())
                        .direction(Short.valueOf(map.get("direction").toString()))
                        .price(new BigDecimal(map.get("price").toString()))
                        .quantity(Integer.valueOf(map.get("quantity").toString()))
                        .tradedQuantity(Integer.valueOf(map.get("tradedQuantity").toString()))
                        .status(Short.valueOf(map.get("status").toString()))
                        .build();
            } catch (Exception e) {
                log.error("反序列化订单失败：{}", json, e);
                return null;
            }
        }

        /**
         * 获取热数据买单队列大小
         *
         * @return 队列大小
         */
        public int getHotBuyQueueSize() {
            return hotBuyOrders.size();
        }

        /**
         * 获取热数据卖单队列大小
         *
         * @return 队列大小
         */
        public int getHotSellQueueSize() {
            return hotSellOrders.size();
        }

        /**
         * 获取冷数据买单数量
         *
         * @return Redis中的买单数量
         */
        public long getColdBuyQueueSize() {
            String redisKey = REDIS_KEY_PREFIX + symbol + REDIS_BUY_KEY_SUFFIX;
            Long size = stringRedisTemplate.opsForZSet().size(redisKey);
            return size != null ? size : 0;
        }

        /**
         * 获取冷数据卖单数量
         *
         * @return Redis中的卖单数量
         */
        public long getColdSellQueueSize() {
            String redisKey = REDIS_KEY_PREFIX + symbol + REDIS_SELL_KEY_SUFFIX;
            Long size = stringRedisTemplate.opsForZSet().size(redisKey);
            return size != null ? size : 0;
        }
    }
}
