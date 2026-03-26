package org.dzianisbova.paymentmock.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {

    public String paymentIntentId() {
        return "pi_mock_" + randomHex();
    }

    public String refundId() {
        return "re_mock_" + randomHex();
    }

    public String eventId() {
        return "evt_mock_" + randomHex();
    }

    private String randomHex() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
