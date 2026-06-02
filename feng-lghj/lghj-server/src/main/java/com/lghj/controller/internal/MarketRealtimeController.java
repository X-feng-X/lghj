package com.lghj.controller.internal;

import com.lghj.pojo.dto.Result;
import com.lghj.pojo.vo.StockNewsVO;
import com.lghj.service.IRealTimeStockService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/market")
@RequiredArgsConstructor
public class MarketRealtimeController {

    private final IRealTimeStockService realTimeStockService;

    @Value("${lghj.internal-api.token:}")
    private String internalApiToken;

    @GetMapping("/realtime")
    public Result queryRealtime(@RequestParam String code,
                                @RequestParam(required = false) String market,
                                @RequestParam(defaultValue = "5") Integer recentNewsSize,
                                @RequestParam(defaultValue = "true") Boolean includeMinute,
                                @RequestHeader(value = "X-Internal-Token", required = false) String token) {
        if (StringUtils.hasText(internalApiToken) && !internalApiToken.equals(token)) {
            return Result.error("internal token invalid");
        }
        if (!StringUtils.hasText(code)) {
            return Result.error("stock code required");
        }

        String normalizedCode = code.trim();
        String normalizedMarket = StringUtils.hasText(market) ? market.trim().toLowerCase() : inferMarket(normalizedCode);
        int newsSize = recentNewsSize == null ? 5 : Math.max(0, Math.min(recentNewsSize, 20));

        MarketRealtimeData data = new MarketRealtimeData();
        data.setMarket(normalizedMarket);
        data.setCode(normalizedCode);
        data.setQuote(realTimeStockService.getRealTimeQuote(normalizedMarket, normalizedCode));
        data.setNews(realTimeStockService.getStockNews(normalizedCode, newsSize));
        data.setQueryTime(LocalDateTime.now().toString());
        if (Boolean.TRUE.equals(includeMinute)) {
            data.setMinuteData(realTimeStockService.getMinuteData(normalizedMarket, normalizedCode));
        }

        return Result.success(data);
    }

    private String inferMarket(String code) {
        if (code.startsWith("5") || code.startsWith("6") || code.startsWith("9")) {
            return "sh";
        }
        return "sz";
    }

    @Data
    public static class MarketRealtimeData {
        private String market;
        private String code;
        private String queryTime;
        private Map<String, Object> quote;
        private List<Map<String, Object>> minuteData;
        private List<StockNewsVO> news;
    }
}
