package org.dzianisbova.paymentmock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "mock")
@Configuration
public class MockPaymentConfig {
    private Webhook webhook = new Webhook();
    private Scenario scenario = new Scenario();

    @Data
    public static class Webhook {
        private String targetUrl;
        private long delayMs;
    }

    @Data
    public static class Scenario {
        private long timeoutDelayMs;
        private long slowDelayMs;
    }
}
