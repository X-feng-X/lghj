package com.lghj.task;

import com.lghj.service.IRealTimeStockService;
import com.lghj.utils.OrderBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * 每 3 秒获取一次活跃股票的最新价格，并推送给 OrderBook
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataSchedulerTask {

    private final OrderBook orderBook;
    private final IRealTimeStockService realTimeStockService;

    /**
     * 每3秒更新一次行情，触发订单撮合
     */
    @Scheduled(fixedRate = 3000)
    public void updateMarketData() {

        Set<String> activeSymbols = orderBook.getActiveSymbols();
        if (activeSymbols == null || activeSymbols.isEmpty()) {
            return;
        }

        log.debug("开始更新行情，活跃股票数量：{}", activeSymbols.size());

        for (String symbol : activeSymbols) {
            try {
                // 获取实时价格
                // 注意：这里需要根据股票代码判断市场（sh/sz），简化处理
                String market = "sz";
                if (symbol.startsWith("60")) {
                    market = "sh";
                }

                Map<String, Object> quote = realTimeStockService.getRealTimeQuote(market, symbol);
                if (quote != null) {
                    Object priceObj = quote.get("price");
                    BigDecimal currentPrice = null;
                    if (priceObj instanceof BigDecimal) {
                        currentPrice = (BigDecimal) priceObj;
                    } else if (priceObj instanceof String) {
                        currentPrice = new BigDecimal((String) priceObj);
                    } else if (priceObj instanceof Double) {
                        currentPrice = BigDecimal.valueOf((Double) priceObj);
                    }

                    if (currentPrice != null) {
                        // 触发撮合
                        orderBook.processMarketData(symbol, currentPrice);
                    }
                }
            } catch (Exception e) {
                log.error("更新行情失败，股票代码：{}", symbol, e);
            }
        }
    }
}
