package cn.feng.domain.agent.service.armory.matter.mcp.server;

import cn.feng.domain.agent.adapter.port.SimTradeProfilePort;
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
public class InvestmentTradeProfileMcpService {

    private final SimTradeProfilePort simTradeProfilePort;

    @Tool(description = "根据当前用户ID查询量股化金模拟交易画像，包括账户、持仓、近期委托、近期成交、交易行为标签和仓位集中度。只用于个性化投资顾问分析。")
    public SimTradeProfileResponse querySimTradeProfile(SimTradeProfileRequest request) {
        SimTradeProfileResponse response = new SimTradeProfileResponse();
        if (request == null || !StringUtils.hasText(request.getUserId())) {
            response.setSuccess(false);
            response.setMessage("缺少当前用户ID，无法查询模拟交易画像。");
            return response;
        }

        String profileJson = simTradeProfilePort.queryProfileJson(request.getUserId());
        if (!StringUtils.hasText(profileJson)) {
            response.setSuccess(false);
            response.setMessage("未获取到模拟交易画像，可能是账户不存在、暂无交易记录或交易系统不可用。");
            return response;
        }

        response.setSuccess(true);
        response.setMessage("OK");
        response.setProfileJson(profileJson);
        return response;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SimTradeProfileRequest {
        @JsonProperty(required = true, value = "userId")
        @JsonPropertyDescription("当前对话用户ID。必须使用系统上下文中的当前用户ID，不要编造或改查其他用户。")
        private String userId;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SimTradeProfileResponse {
        @JsonProperty(required = true, value = "success")
        @JsonPropertyDescription("是否成功获取画像")
        private boolean success;

        @JsonProperty(value = "message")
        @JsonPropertyDescription("查询状态或错误信息")
        private String message;

        @JsonProperty(value = "profileJson")
        @JsonPropertyDescription("量股化金返回的模拟交易画像 JSON，包含 data.account、data.positions、data.recentOrders、data.recentDeals、data.summary")
        private String profileJson;
    }
}
