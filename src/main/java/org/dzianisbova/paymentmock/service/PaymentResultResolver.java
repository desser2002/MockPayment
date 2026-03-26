package org.dzianisbova.paymentmock.service;

import org.dzianisbova.paymentmock.exception.PaymentException;
import org.dzianisbova.paymentmock.model.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentResultResolver {
    public PaymentResult resolve(Map<String, String> metadata) {
        String scenario = metadata != null ? metadata.get("scenario") : null;

        if (scenario == null || scenario.isBlank()) {
            return PaymentResult.SUCCEEDED;
        }

        PaymentResult result = PaymentResult.fromString(scenario);
        if (result == null) {
            throw new PaymentException(422, "invalid_request_error", "invalid_scenario",
                    "Unknown scenario: " + scenario, null, null);
        }
        return result;
    }
}
