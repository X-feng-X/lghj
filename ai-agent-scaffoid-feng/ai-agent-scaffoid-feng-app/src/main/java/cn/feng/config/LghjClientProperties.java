package cn.feng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lghj.client")
public class LghjClientProperties {

    private String baseUrl = "http://127.0.0.1:8080";
    private String internalToken = "";
    private int timeoutSeconds = 5;
}
