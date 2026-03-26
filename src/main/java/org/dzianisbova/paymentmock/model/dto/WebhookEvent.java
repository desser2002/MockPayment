package org.dzianisbova.paymentmock.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public record WebhookEvent(String id, String type,
                           long created, Data data) {
    public record Data(PaymentIntent object) {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record PaymentIntent(String id, String object,
                                    long amount, String currency, String status,
                                    Map<String, String> metadata, LastPaymentError lastPaymentError) {
            public record LastPaymentError(String code, String message) {
            }
        }
    }
}
