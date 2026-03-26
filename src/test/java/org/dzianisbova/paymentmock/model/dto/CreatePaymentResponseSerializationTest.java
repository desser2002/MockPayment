package org.dzianisbova.paymentmock.model.dto;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CreatePaymentResponseSerializationTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void serializesToSnakeCase() {
        var response = new CreatePaymentResponse(
                "pi_mock_123", "payment_intent", 2000, "usd",
                "succeeded", "pi_mock_123_secret_xyz", "pm_card_visa",
                "user@example.com", Map.of("orderId", "order-123"), 1711234567L
        );

        String json = mapper.writeValueAsString(response);

        assertThat(json)
                .contains("client_secret")
                .contains("payment_method")
                .contains("receipt_email")
                .doesNotContain("clientSecret")
                .doesNotContain("paymentMethod")
                .doesNotContain("receiptEmail");
    }
}
