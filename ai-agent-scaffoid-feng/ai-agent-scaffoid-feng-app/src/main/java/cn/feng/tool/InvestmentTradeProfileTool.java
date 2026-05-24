package cn.feng.tool;

import cn.feng.config.LghjClientProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentTradeProfileTool {

    private final LghjClientProperties properties;

    @Tool(description = "根据当前用户ID查询量股化金模拟交易画像，包括账户、持仓、近期委托、近期成交、交易行为标签和仓位集中度。只用于个性化投资顾问分析。")
    public SimTradeProfileResponse querySimTradeProfile(SimTradeProfileRequest request) {
        SimTradeProfileResponse response = new SimTradeProfileResponse();
        if (request == null || !StringUtils.hasText(request.getUserId())) {
            response.setSuccess(false);
            response.setMessage("缺少当前用户ID，无法查询模拟交易画像。");
            return response;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                    .path("/api/internal/sim-trade/profile")
                    .queryParam("userId", request.getUserId())
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.hasText(properties.getInternalToken())) {
                headers.set("X-Internal-Token", properties.getInternalToken());
            }

            RestTemplate restTemplate = new RestTemplateBuilderFactory(properties.getTimeoutSeconds()).build();
            ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            response.setSuccess(entity.getStatusCode().is2xxSuccessful());
            response.setMessage(entity.getStatusCode().toString());
            response.setProfileJson(entity.getBody());
            return response;
        } catch (Exception e) {
            log.warn("Failed to query simulated trade profile, userId={}", request.getUserId(), e);
            response.setSuccess(false);
            response.setMessage("查询模拟交易画像失败：" + e.getMessage());
            return response;
        }
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

    private static class RestTemplateBuilderFactory {
        private final int timeoutSeconds;

        private RestTemplateBuilderFactory(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        private RestTemplate build() {
            org.springframework.boot.web.client.RestTemplateBuilder builder = new org.springframework.boot.web.client.RestTemplateBuilder()
                    .setConnectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .setReadTimeout(Duration.ofSeconds(timeoutSeconds));
            return builder.build();
        }
    }
}
