package org.dzianisbova.paymentmock.model.dto;

public record RefundResponse(String id, String object, long amount,
                             String currency, String paymentIntent,
                             String status, long created) {
}
