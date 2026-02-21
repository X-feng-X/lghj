package com.lghj.utils;

import com.lghj.pojo.entity.TradeOrder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Slf4j
@Component
public class OrderBook {

    // 订单处理器，由外部注入
    @Setter
    private OrderQueueManager.OrderProcessor orderProcessor;

    // 为每个股票维护一个订单簿
    private final Map<String, StockOrderBook> stockOrderBooks = new ConcurrentHashMap<>();

    /**
     * 获取所有活跃的股票代码
     *
     * @return 股票代码集合
     */
    public Set<String> getActiveSymbols() {
        return stockOrderBooks.keySet();
    }

    /**
     * 获取股票的订单簿
     *
     * @param symbol 股票代码
     * @return 股票订单簿
     */
    public StockOrderBook getStockOrderBook(String symbol) {
        return stockOrderBooks.computeIfAbsent(symbol, k -> new StockOrderBook());
    }

    /**
     * 添加限价单到订单簿
     *
     * @param order 限价单
     */
    public void addLimitOrder(TradeOrder order) {
        StockOrderBook orderBook = getStockOrderBook(order.getSymbol());
        orderBook.addOrder(order);
        log.info("限价单已添加到订单簿，股票代码：{}，委托单号：{}，价格：{}，数量：{}",
                order.getSymbol(), order.getOrderNo(), order.getPrice(), order.getQuantity());
    }

    /**
     * 从订单簿中移除订单
     *
     * @param order 订单
     */
    public void removeOrder(TradeOrder order) {
        StockOrderBook orderBook = stockOrderBooks.get(order.getSymbol());
        if (orderBook != null) {
            orderBook.removeOrder(order);
            log.info("订单已从订单簿中移除，股票代码：{}，委托单号：{}", order.getSymbol(), order.getOrderNo());
        }
    }

    /**
     * 处理行情更新，驱动限价单撮合
     *
     * @param symbol 股票代码
     * @param currentPrice 当前价格
     */
    public void processMarketData(String symbol, BigDecimal currentPrice) {
        StockOrderBook orderBook = stockOrderBooks.get(symbol);
        if (orderBook == null) {
            return;
        }

        // log.info("处理行情更新，股票代码：{}，当前价格：{}", symbol, currentPrice);
        orderBook.processMarketData(currentPrice, this.orderProcessor);
    }

    /**
     * 股票订单簿，管理单个股票的限价单
     */
    public static class StockOrderBook {

        // 买单队列，价格从高到低排序
        private final PriorityBlockingQueue<TradeOrder> buyOrders = new PriorityBlockingQueue<>(10,
                (o1, o2) -> o2.getPrice().compareTo(o1.getPrice()));

        // 卖单队列，价格从低到高排序
        private final PriorityBlockingQueue<TradeOrder> sellOrders = new PriorityBlockingQueue<>(10,
                Comparator.comparing(TradeOrder::getPrice));

        /**
         * 添加订单到订单簿
         *
         * @param order 订单
         */
        public void addOrder(TradeOrder order) {
            if (order.getDirection() == 1) { // 买入
                buyOrders.offer(order);
            } else if (order.getDirection() == 2) { // 卖出
                sellOrders.offer(order);
            }
        }

        /**
         * 从订单簿中移除订单
         *
         * @param order 订单
         */
        public void removeOrder(TradeOrder order) {
            if (order.getDirection() == 1) { // 买入
                buyOrders.remove(order);
            } else if (order.getDirection() == 2) { // 卖出
                sellOrders.remove(order);
            }
        }

        /**
         * 处理行情更新，驱动限价单撮合
         *
         * @param currentPrice 当前价格
         * @param processor 订单处理器
         */
        public void processMarketData(BigDecimal currentPrice, OrderQueueManager.OrderProcessor processor) {

            // 处理买单：价格大于等于当前价格的买单可以成交
            List<TradeOrder> executableBuyOrders = new ArrayList<>();
            // 使用peek查看队首元素，不移除
            while (true) {
                TradeOrder buyOrder = buyOrders.peek();
                if (buyOrder == null) {
                    break;
                }
                if (buyOrder.getPrice().compareTo(currentPrice) >= 0) {
                    // 只有能成交才从队列移除
                    buyOrders.poll();
                    executableBuyOrders.add(buyOrder);
                } else {
                    // 由于买单是按价格从高到低排序的，所以后续买单价格更低，无需继续遍历
                    break;
                }
            }

            // 处理卖单：价格小于等于当前价格的卖单可以成交
            List<TradeOrder> executableSellOrders = new ArrayList<>();
            while (true) {
                TradeOrder sellOrder = sellOrders.peek();
                if (sellOrder == null) {
                    break;
                }
                if (sellOrder.getPrice().compareTo(currentPrice) <= 0) {
                    // 只有能成交才从队列移除
                    sellOrders.poll();
                    executableSellOrders.add(sellOrder);
                } else {
                    // 由于卖单是按价格从低到高排序的，所以后续卖单价格更高，无需继续遍历
                    break;
                }
            }

            // 执行可成交的订单
            for (TradeOrder buyOrder : executableBuyOrders) {
                log.info("买单可成交，股票代码：{}，委托单号：{}，委托价格：{}，当前价格：{}",
                        buyOrder.getSymbol(), buyOrder.getOrderNo(), buyOrder.getPrice(), currentPrice);
                // 调用外部注入的处理器执行订单
                if (processor != null) {
                    processor.processOrder(buyOrder);
                }
            }

            for (TradeOrder sellOrder : executableSellOrders) {
                log.info("卖单可成交，股票代码：{}，委托单号：{}，委托价格：{}，当前价格：{}",
                        sellOrder.getSymbol(), sellOrder.getOrderNo(), sellOrder.getPrice(), currentPrice);
                // 调用外部注入的处理器执行订单
                if (processor != null) {
                    processor.processOrder(sellOrder);
                }
            }
        }

        /**
         * 获取买单队列
         *
         * @return 买单队列
         */
        public PriorityBlockingQueue<TradeOrder> getBuyOrders() {
            return buyOrders;
        }

        /**
         * 获取卖单队列
         *
         * @return 卖单队列
         */
        public PriorityBlockingQueue<TradeOrder> getSellOrders() {
            return sellOrders;
        }
    }
}
