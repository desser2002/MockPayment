package org.dzianisbova.paymentmock.model.dto;

import java.util.Map;

public record CreatePaymentRequest(long amount, String currency, String paymentMethod,
                                   String receiptEmail, Map<String, String> metadata) {
}
