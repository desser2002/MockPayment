package org.dzianisbova.paymentmock.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPaymentIntent_validRequest_returnsSucceeded() throws Exception {
        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "2000")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa")
                        .param("receipt_email", "user@example.com")
                        .param("metadata[orderId]", "order-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.startsWith("pi_mock_")))
                .andExpect(jsonPath("$.object").value("payment_intent"))
                .andExpect(jsonPath("$.amount").value(2000))
                .andExpect(jsonPath("$.currency").value("usd"))
                .andExpect(jsonPath("$.status").value("succeeded"))
                .andExpect(jsonPath("$.client_secret").exists())
                .andExpect(jsonPath("$.payment_method").value("pm_card_visa"))
                .andExpect(jsonPath("$.receipt_email").value("user@example.com"))
                .andExpect(jsonPath("$.metadata.orderId").value("order-123"))
                .andExpect(jsonPath("$.created").isNumber());
    }

    @Test
    void createPaymentIntent_scenarioDeclined_returns400CardError() throws Exception {
        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "2000")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa")
                        .param("metadata[scenario]", "declined"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("card_error"))
                .andExpect(jsonPath("$.error.code").value("card_declined"))
                .andExpect(jsonPath("$.error.message").value("Your card was declined."))
                .andExpect(jsonPath("$.error.payment_intent.id").value(org.hamcrest.Matchers.startsWith("pi_mock_")))
                .andExpect(jsonPath("$.error.payment_intent.status").value("requires_payment_method"));
    }

    @Test
    void createPaymentIntent_scenarioServerError_returns500ApiError() throws Exception {
        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "2000")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa")
                        .param("metadata[scenario]", "server_error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.type").value("api_error"))
                .andExpect(jsonPath("$.error.message").value("An error occurred on our server. Please try again."));
    }

    @Test
    void createPaymentIntent_amountZero_returns422AmountTooSmall() throws Exception {
        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "0")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.type").value("invalid_request_error"))
                .andExpect(jsonPath("$.error.code").value("amount_too_small"))
                .andExpect(jsonPath("$.error.message").value("Amount must be greater than 0."));
    }

    @Test
    void createPaymentIntent_amountNegative_returns422AmountTooSmall() throws Exception {
        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "-100")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.type").value("invalid_request_error"))
                .andExpect(jsonPath("$.error.code").value("amount_too_small"))
                .andExpect(jsonPath("$.error.message").value("Amount must be greater than 0."));
    }

    @Test
    void createPaymentIntent_amountNotANumber_returns422InvalidAmount() throws Exception {
        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "abc")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.type").value("invalid_request_error"))
                .andExpect(jsonPath("$.error.code").value("invalid_amount"))
                .andExpect(jsonPath("$.error.message").value("Amount must be a valid integer."));
    }

    @Test
    void getPaymentIntent_existingId_returns200WithData() throws Exception {
        String createResponse = mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", "3000")
                        .param("currency", "eur")
                        .param("payment_method", "pm_card_visa")
                        .param("receipt_email", "test@example.com")
                        .param("metadata[orderId]", "order-456"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        String id = mapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/v1/payment_intents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value(3000))
                .andExpect(jsonPath("$.currency").value("eur"))
                .andExpect(jsonPath("$.status").value("succeeded"));
    }

    @Test
    void getPaymentIntent_nonExistingId_returns404ResourceMissing() throws Exception {
        mockMvc.perform(get("/v1/payment_intents/pi_mock_nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.type").value("invalid_request_error"))
                .andExpect(jsonPath("$.error.code").value("resource_missing"));
    }

    @Test
    void refundPaymentIntent_fullRefund_returns200WithFullAmount() throws Exception {
        String id = createPaymentAndReturnId("2000");

        mockMvc.perform(post("/v1/payment_intents/{id}/refund", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.startsWith("re_mock_")))
                .andExpect(jsonPath("$.object").value("refund"))
                .andExpect(jsonPath("$.amount").value(2000))
                .andExpect(jsonPath("$.currency").value("usd"))
                .andExpect(jsonPath("$.payment_intent").value(id))
                .andExpect(jsonPath("$.status").value("succeeded"))
                .andExpect(jsonPath("$.created").isNumber());
    }

    @Test
    void refundPaymentIntent_partialRefund_returns200WithPartialAmount() throws Exception {
        String id = createPaymentAndReturnId("2000");

        mockMvc.perform(post("/v1/payment_intents/{id}/refund", id)
                        .param("amount", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500))
                .andExpect(jsonPath("$.payment_intent").value(id));
    }

    @Test
    void refundPaymentIntent_nonExistingId_returns404ResourceMissing() throws Exception {
        mockMvc.perform(post("/v1/payment_intents/pi_mock_nonexistent/refund"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.type").value("invalid_request_error"))
                .andExpect(jsonPath("$.error.code").value("resource_missing"));
    }

    @Test
    void refundPaymentIntent_amountExceedsPayment_returns400AmountTooLarge() throws Exception {
        String id = createPaymentAndReturnId("2000");

        mockMvc.perform(post("/v1/payment_intents/{id}/refund", id)
                        .param("amount", "3000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.type").value("invalid_request_error"))
                .andExpect(jsonPath("$.error.code").value("amount_too_large"));
    }

    @Test
    void createPaymentIntent_headerSlow_respondsWithDelayAndSucceeds() throws Exception {
        long start = System.currentTimeMillis();

        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-Mock-Scenario", "slow")
                        .param("amount", "2000")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("succeeded"));

        assertThat(System.currentTimeMillis() - start).isGreaterThanOrEqualTo(100);
    }

    @Test
    void createPaymentIntent_headerTimeout_respondsWithDelayAndSucceeds() throws Exception {
        long start = System.currentTimeMillis();

        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-Mock-Scenario", "timeout")
                        .param("amount", "2000")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("succeeded"));

        assertThat(System.currentTimeMillis() - start).isGreaterThanOrEqualTo(200);
    }

    @Test
    void createPaymentIntent_unknownLatencyScenario_returns422() throws Exception {
        mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-Mock-Scenario", "unknown")
                        .param("amount", "2000")
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.type").value("invalid_request_error"))
                .andExpect(jsonPath("$.error.code").value("invalid_latency_scenario"));
    }

    private String createPaymentAndReturnId(String amount) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String response = mockMvc.perform(post("/v1/payment_intents")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("amount", amount)
                        .param("currency", "usd")
                        .param("payment_method", "pm_card_visa"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asText();
    }
}
