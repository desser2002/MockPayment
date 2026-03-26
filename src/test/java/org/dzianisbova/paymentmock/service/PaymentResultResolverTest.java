package org.dzianisbova.paymentmock.service;

import org.dzianisbova.paymentmock.exception.PaymentException;
import org.dzianisbova.paymentmock.model.PaymentResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentResultResolverTest {

    private final PaymentResultResolver resolver = new PaymentResultResolver();

    @Test
    void resolve_noMetadata_returnsSucceeded() {
        assertThat(resolver.resolve(null)).isEqualTo(PaymentResult.SUCCEEDED);
    }

    @Test
    void resolve_emptyMetadata_returnsSucceeded() {
        assertThat(resolver.resolve(Map.of())).isEqualTo(PaymentResult.SUCCEEDED);
    }

    @Test
    void resolve_scenarioSucceeded_returnsSucceeded() {
        assertThat(resolver.resolve(Map.of("scenario", "succeeded"))).isEqualTo(PaymentResult.SUCCEEDED);
    }

    @Test
    void resolve_scenarioDeclined_returnsDeclined() {
        assertThat(resolver.resolve(Map.of("scenario", "declined"))).isEqualTo(PaymentResult.DECLINED);
    }

    @Test
    void resolve_scenarioServerError_returnsServerError() {
        assertThat(resolver.resolve(Map.of("scenario", "server_error"))).isEqualTo(PaymentResult.SERVER_ERROR);
    }

    @Test
    void resolve_unknownScenario_throws422() {
        assertThatThrownBy(() -> resolver.resolve(Map.of("scenario", "unknown")))
                .isInstanceOf(PaymentException.class)
                .satisfies(ex -> {
                    PaymentException paymentException = (PaymentException) ex;
                    assertThat(paymentException.getHttpStatus()).isEqualTo(422);
                    assertThat(paymentException.getCode()).isEqualTo("invalid_scenario");
                });
    }
}
