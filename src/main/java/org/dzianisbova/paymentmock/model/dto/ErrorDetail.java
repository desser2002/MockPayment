package org.dzianisbova.paymentmock.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(String type, String code,
                          String message, PaymentIntentInfo paymentIntent) {
    public record PaymentIntentInfo(String id, String status) {
    }
}
