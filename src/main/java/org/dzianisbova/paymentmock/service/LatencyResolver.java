package org.dzianisbova.paymentmock.service;

import org.dzianisbova.paymentmock.exception.PaymentException;
import org.dzianisbova.paymentmock.model.Latency;
import org.springframework.stereotype.Component;

@Component
public class LatencyResolver {

    public Latency resolve(String xMockScenarioHeader) {
        if (xMockScenarioHeader == null || xMockScenarioHeader.isBlank()) {
            return Latency.NONE;
        }

        return switch (xMockScenarioHeader.toLowerCase()) {
            case "slow" -> Latency.SLOW;
            case "timeout" -> Latency.TIMEOUT;
            default -> throw new PaymentException(422, "invalid_request_error", "invalid_latency_scenario",
                    "Unknown latency scenario: " + xMockScenarioHeader, null, null);
        };
    }
}
