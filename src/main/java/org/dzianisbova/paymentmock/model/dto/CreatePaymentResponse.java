package org.dzianisbova.paymentmock.model.dto;

import java.util.Map;

public record CreatePaymentResponse(String id, String object, long amount,
                                    String currency, String status, String clientSecret,
                                    String paymentMethod, String receiptEmail,
                                    Map<String,String> metadata, long created) {
}
