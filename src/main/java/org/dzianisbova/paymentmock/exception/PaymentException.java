package org.dzianisbova.paymentmock.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final int httpStatus;
    private final String type;
    private final String code;
    private final String paymentIntentId;
    private final String paymentIntentStatus;

    public PaymentException(int httpStatus, String type, String code,
                            String message, String paymentIntentId,
                            String paymentIntentStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.type = type;
        this.code = code;
        this.paymentIntentId = paymentIntentId;
        this.paymentIntentStatus = paymentIntentStatus;
    }
}
