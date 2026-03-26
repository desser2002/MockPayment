package org.dzianisbova.paymentmock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dzianisbova.paymentmock.config.MockPaymentConfig;
import org.dzianisbova.paymentmock.model.dto.WebhookEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookDispatcher {

    private final MockPaymentConfig config;
    private final RestClient restClient;

    @Async
    public void dispatch(WebhookEvent event) {
        String targetUrl = config.getWebhook().getTargetUrl();

        if (targetUrl == null || targetUrl.isBlank()) {
            log.warn("Webhook target URL is not configured, skipping dispatch for event {}", event.id());
            return;
        }

        try {
            Thread.sleep(config.getWebhook().getDelayMs());
            restClient.post()
                    .uri(targetUrl)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Webhook dispatched: {} to {}", event.type(), targetUrl);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Webhook dispatch interrupted for event {}", event.id());
        } catch (Exception e) {
            log.error("Failed to dispatch webhook event {} to {}: {}", event.id(), targetUrl, e.getMessage());
        }
    }
}
