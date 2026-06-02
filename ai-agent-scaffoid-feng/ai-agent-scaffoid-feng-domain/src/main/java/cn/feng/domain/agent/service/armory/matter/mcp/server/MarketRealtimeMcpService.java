package cn.feng.domain.agent.service.armory.matter.mcp.server;

import cn.feng.domain.agent.adapter.port.MarketDataPort;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketRealtimeMcpService {

    private final MarketDataPort marketDataPort;

    @Tool(description = "查询A股实时行情、当日分时和最近资讯。用户询问现在价格、涨跌幅、今日走势、实时行情、最新新闻时必须调用。")
    public MarketRealtimeResponse queryRealtimeMarket(MarketRealtimeRequest request) {
        MarketRealtimeResponse response = new MarketRealtimeResponse();
        if (request == null || !StringUtils.hasText(request.getCode())) {
            response.setSuccess(false);
            response.setMessage("缺少股票代码，无法查询实时行情。");
            return response;
        }

        String code = request.getCode().trim();
        String market = StringUtils.hasText(request.getMarket()) ? request.getMarket().trim().toLowerCase() : inferMarket(code);
        int newsSize = request.getRecentNewsSize() == null ? 5 : Math.max(0, Math.min(request.getRecentNewsSize(), 20));
        boolean includeMinute = request.getIncludeMinute() == null || request.getIncludeMinute();

        String marketJson = marketDataPort.queryRealtimeMarketJson(market, code, newsSize, includeMinute);
        if (!StringUtils.hasText(marketJson)) {
            response.setSuccess(false);
            response.setMessage("未获取到实时行情，可能是行情源、主后端或股票代码不可用。");
            return response;
        }

        response.setSuccess(true);
        response.setMessage("OK");
        response.setMarketJson(marketJson);
        return response;
    }

    private String inferMarket(String code) {
        if (code.startsWith("5") || code.startsWith("6") || code.startsWith("9")) {
            return "sh";
        }
        return "sz";
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MarketRealtimeRequest {

        @JsonProperty(required = true, value = "code")
        @JsonPropertyDescription("A股股票代码，例如 600519、000001。不要带市场前缀。")
        private String code;

        @JsonProperty(value = "market")
        @JsonPropertyDescription("市场代码，上海填 sh，深圳填 sz。未知可不填，工具会根据股票代码推断。")
        private String market;

        @JsonProperty(value = "recentNewsSize")
        @JsonPropertyDescription("最近资讯条数，默认5，最大20。")
        private Integer recentNewsSize;

        @JsonProperty(value = "includeMinute")
        @JsonPropertyDescription("是否返回当日分时数据，默认 true。只问新闻时可填 false。")
        private Boolean includeMinute;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MarketRealtimeResponse {

        @JsonProperty(required = true, value = "success")
        @JsonPropertyDescription("是否成功获取实时行情")
        private boolean success;

        @JsonProperty(value = "message")
        @JsonPropertyDescription("查询状态或错误信息")
        private String message;

        @JsonProperty(value = "marketJson")
        @JsonPropertyDescription("主后端返回的实时行情JSON，包含 data.quote、data.minuteData、data.news、data.queryTime")
        private String marketJson;
    }
}
