package cn.feng.adapter.port;

import cn.feng.config.LghjClientProperties;
import cn.feng.domain.agent.adapter.port.MarketDataPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
public class HttpMarketDataPort implements MarketDataPort {

    private final LghjClientProperties properties;

    @Override
    public String queryRealtimeMarketJson(String market, String code, int recentNewsSize, boolean includeMinute) {
        if (!StringUtils.hasText(code)) {
            return "";
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                    .path("/api/internal/market/realtime")
                    .queryParam("market", market)
                    .queryParam("code", code)
                    .queryParam("recentNewsSize", recentNewsSize)
                    .queryParam("includeMinute", includeMinute)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.hasText(properties.getInternalToken())) {
                headers.set("X-Internal-Token", properties.getInternalToken());
            }

            RestTemplate restTemplate = new RestTemplateBuilder()
                    .setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .build();

            ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return entity.getBody();
        } catch (Exception e) {
            log.warn("query realtime market failed, market={}, code={}", market, code, e);
            return "";
        }
    }
}
