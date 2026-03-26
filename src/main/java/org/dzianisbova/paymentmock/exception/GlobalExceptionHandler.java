package org.dzianisbova.paymentmock.exception;

import lombok.RequiredArgsConstructor;
import org.dzianisbova.paymentmock.model.dto.ErrorResponse;
import org.dzianisbova.paymentmock.service.ResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ResponseFactory responseFactory;

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException e) {
        ErrorResponse response = responseFactory.createErrorResponse(
                e.getType(),
                e.getCode(),
                e.getMessage(),
                e.getPaymentIntentId(),
                e.getPaymentIntentStatus()

        );
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleNumberFormatException(NumberFormatException e) {
        ErrorResponse response = responseFactory.createErrorResponse(
                "invalid_request_error", "invalid_amount",
                "Amount must be a valid integer.", null, null
        );

        return ResponseEntity.status(422).body(response);
    }
}
