package org.dzianisbova.paymentmock.service;

import lombok.RequiredArgsConstructor;
import org.dzianisbova.paymentmock.exception.PaymentException;
import org.dzianisbova.paymentmock.model.dto.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ResponseFactory {

    public static final String INVALID_REQUEST_ERROR = "invalid_request_error";

    private final IdGenerator idGenerator;

    public CreatePaymentResponse createSuccessResponse(CreatePaymentRequest request) {
        String id = idGenerator.paymentIntentId();
        return new CreatePaymentResponse(
                id,
                "payment_intent",
                request.amount(),
                request.currency(),
                "succeeded",
                id + "_secret_" + idGenerator.paymentIntentId(),
                request.paymentMethod(),
                request.receiptEmail(),
                request.metadata(),
                Instant.now().getEpochSecond()
        );
    }

    public RefundResponse createRefundResponse(String paymentIntentId, long amount, String currency) {
        return new RefundResponse(
                idGenerator.refundId(),
                "refund",
                amount,
                currency,
                paymentIntentId,
                "succeeded",
                Instant.now().getEpochSecond()
        );
    }

    public ErrorResponse createErrorResponse(String type, String code, String message,
                                             String paymentIntentId, String status) {
        ErrorDetail.PaymentIntentInfo intentInfo = paymentIntentId != null
                ? new ErrorDetail.PaymentIntentInfo(paymentIntentId, status)
                : null;
        return new ErrorResponse(new ErrorDetail(type, code, message, intentInfo));
    }

    public PaymentException createDeclinedException() {
        return new PaymentException(400, "card_error", "card_declined",
                "Your card was declined.", idGenerator.paymentIntentId(), "requires_payment_method");
    }

    public PaymentException createServerErrorException() {
        return new PaymentException(500, "api_error", null,
                "An error occurred on our server. Please try again.", null, null);
    }

    public PaymentException createAmountTooSmallException() {
        return new PaymentException(422, INVALID_REQUEST_ERROR, "amount_too_small",
                "Amount must be greater than 0.", null, null);
    }

    public PaymentException createNotFoundException(String paymentIntentId) {
        return new PaymentException(404, INVALID_REQUEST_ERROR, "resource_missing",
                "No such payment intent: " + paymentIntentId, null, null);
    }

    public PaymentException createRefundAmountTooLargeException() {
        return new PaymentException(400, INVALID_REQUEST_ERROR, "amount_too_large",
                "Refund amount exceeds the payment amount.", null, null);
    }
}
