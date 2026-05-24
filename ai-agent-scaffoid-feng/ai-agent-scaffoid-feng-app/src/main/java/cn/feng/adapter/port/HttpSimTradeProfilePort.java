package cn.feng.adapter.port;

import cn.feng.config.LghjClientProperties;
import cn.feng.domain.agent.adapter.port.SimTradeProfilePort;
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
public class HttpSimTradeProfilePort implements SimTradeProfilePort {

    private final LghjClientProperties properties;

    @Override
    public String queryProfileJson(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "";
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                    .path("/api/internal/sim-trade/profile")
                    .queryParam("userId", userId)
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
            log.warn("query simulated trade profile failed, userId={}", userId, e);
            return "";
        }
    }
}
