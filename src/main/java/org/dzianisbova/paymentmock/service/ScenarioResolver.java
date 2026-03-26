package org.dzianisbova.paymentmock.service;

import lombok.RequiredArgsConstructor;
import org.dzianisbova.paymentmock.model.Latency;
import org.dzianisbova.paymentmock.model.MockScenario;
import org.dzianisbova.paymentmock.model.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ScenarioResolver {

    private final PaymentResultResolver paymentResultResolver;
    private final LatencyResolver latencyResolver;

    public MockScenario resolve(String xMockScenarioHeader, Map<String, String> metadata) {
        PaymentResult paymentResult = paymentResultResolver.resolve(metadata);
        Latency latency = latencyResolver.resolve(xMockScenarioHeader);
        return new MockScenario(paymentResult, latency);
    }
}
