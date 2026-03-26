package org.dzianisbova.paymentmock.service;

import lombok.RequiredArgsConstructor;
import org.dzianisbova.paymentmock.config.MockPaymentConfig;
import org.dzianisbova.paymentmock.exception.PaymentException;
import org.dzianisbova.paymentmock.model.Latency;
import org.dzianisbova.paymentmock.model.MockScenario;
import org.dzianisbova.paymentmock.model.PaymentResult;
import org.dzianisbova.paymentmock.model.dto.CreatePaymentRequest;
import org.dzianisbova.paymentmock.model.dto.CreatePaymentResponse;
import org.dzianisbova.paymentmock.model.dto.RefundResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PaymentIntentService {

    private final ScenarioResolver scenarioResolver;
    private final ResponseFactory responseFactory;
    private final MockPaymentConfig config;
    private final WebhookDispatcher webhookDispatcher;
    private final WebhookEventFactory webhookEventFactory;
    private final Map<String, CreatePaymentResponse> storage = new ConcurrentHashMap<>();

    public CreatePaymentResponse createPayment(CreatePaymentRequest request, String xMockScenarioHeader) {
        if (request.amount() <= 0) {
            throw responseFactory.createAmountTooSmallException();
        }

        MockScenario scenario = scenarioResolver.resolve(xMockScenarioHeader, request.metadata());
        applyLatency(scenario.latency());
        return processPaymentResult(scenario.paymentResult(), request);
    }

    public RefundResponse refundPayment(String paymentIntentId, Long amount) {
        CreatePaymentResponse payment = storage.get(paymentIntentId);
        if (payment == null) {
            throw responseFactory.createNotFoundException(paymentIntentId);
        }
        long refundAmount = amount != null ? amount : payment.amount();
        if (refundAmount > payment.amount()) {
            throw responseFactory.createRefundAmountTooLargeException();
        }
        return responseFactory.createRefundResponse(paymentIntentId, refundAmount, payment.currency());
    }

    public CreatePaymentResponse getPayment(String paymentIntentId) {
        CreatePaymentResponse response = storage.get(paymentIntentId);
        if (response == null) {
            throw responseFactory.createNotFoundException(paymentIntentId);
        }
        return response;
    }

    private CreatePaymentResponse processPaymentResult(PaymentResult paymentResult, CreatePaymentRequest request) {
        return switch (paymentResult) {
            case SUCCEEDED -> {
                CreatePaymentResponse response = responseFactory.createSuccessResponse(request);
                storage.put(response.id(), response);
                webhookDispatcher.dispatch(webhookEventFactory.createSucceededEvent(response));
                yield response;
            }
            case DECLINED -> {
                PaymentException exception = responseFactory.createDeclinedException();
                webhookDispatcher.dispatch(webhookEventFactory.createFailedEvent(
                        exception.getPaymentIntentId(), request.metadata()));
                throw exception;
            }
            case SERVER_ERROR -> throw responseFactory.createServerErrorException();
        };
    }

    private void applyLatency(Latency latency) {
        long delayMs = switch (latency) {
            case SLOW -> config.getScenario().getSlowDelayMs();
            case TIMEOUT -> config.getScenario().getTimeoutDelayMs();
            case NONE -> 0;
        };
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
