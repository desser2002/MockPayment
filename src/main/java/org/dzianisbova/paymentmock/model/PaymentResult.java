package org.dzianisbova.paymentmock.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PaymentResult {
    SUCCEEDED,
    DECLINED,
    SERVER_ERROR;

    private static final Map<String, PaymentResult> PAYMENT_RESULTS = Arrays.stream(values())
            .collect(Collectors.toMap(
                    s -> s.name().toLowerCase(),
                    s -> s
            ));

    public static PaymentResult fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return PAYMENT_RESULTS.get(value.toLowerCase());
    }
}
