package org.dzianisbova.paymentmock.service;

import lombok.RequiredArgsConstructor;
import org.dzianisbova.paymentmock.model.dto.CreatePaymentResponse;
import org.dzianisbova.paymentmock.model.dto.WebhookEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebhookEventFactory {

    private final IdGenerator idGenerator;

    public WebhookEvent createSucceededEvent(CreatePaymentResponse payment) {
        WebhookEvent.Data.PaymentIntent intent = new WebhookEvent.Data.PaymentIntent(
                payment.id(), "payment_intent",
                payment.amount(), payment.currency(), "succeeded",
                payment.metadata(), null
        );
        return new WebhookEvent(idGenerator.eventId(), "payment_intent.succeeded",
                Instant.now().getEpochSecond(), new WebhookEvent.Data(intent));
    }

    public WebhookEvent createFailedEvent(String paymentIntentId, Map<String, String> metadata) {
        WebhookEvent.Data.PaymentIntent.LastPaymentError error =
                new WebhookEvent.Data.PaymentIntent.LastPaymentError("card_declined", "Your card was declined.");

        WebhookEvent.Data.PaymentIntent intent = new WebhookEvent.Data.PaymentIntent(
                paymentIntentId, "payment_intent",
                0, null, "requires_payment_method",
                metadata, error
        );
        return new WebhookEvent(idGenerator.eventId(), "payment_intent.payment_failed",
                Instant.now().getEpochSecond(), new WebhookEvent.Data(intent));
    }
}
