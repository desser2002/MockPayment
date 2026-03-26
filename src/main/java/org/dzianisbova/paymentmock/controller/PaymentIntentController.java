package org.dzianisbova.paymentmock.controller;

import lombok.RequiredArgsConstructor;
import org.dzianisbova.paymentmock.model.dto.CreatePaymentRequest;
import org.dzianisbova.paymentmock.model.dto.CreatePaymentResponse;
import org.dzianisbova.paymentmock.model.dto.RefundResponse;
import org.dzianisbova.paymentmock.service.PaymentIntentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/payment_intents")
@RequiredArgsConstructor
public class PaymentIntentController {
    private final PaymentIntentService paymentIntentService;

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<CreatePaymentResponse> createPaymentIntent(
            @RequestHeader(value = "X-Mock-Scenario", required = false) String xMockScenario,
            @RequestParam Map<String, String> params) {

        CreatePaymentRequest request = new CreatePaymentRequest(
                Long.parseLong(params.get("amount")),
                params.get("currency"),
                params.get("payment_method"),
                params.get("receipt_email"),
                extractMetadata(params)
        );
        return ResponseEntity.ok(paymentIntentService.createPayment(request, xMockScenario));
    }

    @GetMapping("/{paymentIntentId}")
    public ResponseEntity<CreatePaymentResponse> getPaymentIntent(
            @PathVariable String paymentIntentId) {
        return ResponseEntity.ok(paymentIntentService.getPayment(paymentIntentId));
    }

    @PostMapping("/{paymentIntentId}/refund")
    public ResponseEntity<RefundResponse> refundPaymentIntent(
            @PathVariable String paymentIntentId,
            @RequestParam(required = false) Long amount) {
        return ResponseEntity.ok(paymentIntentService.refundPayment(paymentIntentId, amount));
    }

    private Map<String, String> extractMetadata(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getKey().startsWith("metadata["))
                .collect(Collectors.toMap(
                        e -> e.getKey().substring(9, e.getKey().length() - 1),
                        Map.Entry::getValue
                ));
    }
}
