# MockPayment

A mock payment service that simulates the Stripe Payment Intents API. It is designed for local development and integration testing — allowing you to create payments, trigger failures, and receive webhooks without touching a real payment provider.

---

## What this project is and why it exists

Real payment APIs require credentials, sandbox accounts, and network access. This service removes those dependencies by providing a fully controllable in-memory stub that speaks the same contract as Stripe's Payment Intents API.

---

## What it does

- Creates payment intents and stores them in memory
- Retrieves payment intents by ID
- Processes full or partial refunds
- Dispatches asynchronous webhook events (`payment_intent.succeeded`, `payment_intent.payment_failed`)
- Simulates payment outcomes (success, card decline, server error) via request metadata
- Simulates network latency (slow response, timeout) via a request header

---

## What it does NOT do

- No authentication or API keys
- No persistent storage (data is lost on restart)
- No currency conversion or amount formatting
- No partial captures, setup intents, or payment methods management
- No real card validation
- No idempotency keys
- Webhook delivery is fire-and-forget (no retries)

---

## API Contract

Base path: `/v1/payment_intents`  
All request bodies use `application/x-www-form-urlencoded`. All responses are JSON.

### `POST /v1/payment_intents`

Creates a payment intent.

**Request fields**

| Field | Type | Required | Description |
|---|---|---|---|
| `amount` | integer | ✅ | Amount in the smallest currency unit (e.g. cents) |
| `currency` | string | ✅ | Currency code (e.g. `usd`) |
| `payment_method` | string | ✅ | Payment method token (e.g. `pm_card_visa`) |
| `receipt_email` | string | ❌ | Email to send receipt to |
| `metadata[scenario]` | string | ❌ | Controls outcome. One of `succeeded` (default), `declined`, `server_error` |

**Response** — `200 OK`

```json
{
  "id": "pi_mock_...",
  "object": "payment_intent",
  "amount": 1000,
  "currency": "usd",
  "status": "succeeded",
  "client_secret": "pi_mock_..._secret_...",
  "payment_method": "pm_card_visa",
  "receipt_email": "user@example.com",
  "metadata": { "scenario": "succeeded" },
  "created": 1700000000
}
```

---

### `GET /v1/payment_intents/{id}`

Retrieves a previously created payment intent.

**Response** — `200 OK` — same shape as the create response.

---

### `POST /v1/payment_intents/{id}/refund`

Refunds a payment intent, fully or partially.

**Request fields**

| Field | Type | Required | Description |
|---|---|---|---|
| `amount` | integer | ❌ | Amount to refund. Defaults to the full payment amount. |

**Response** — `200 OK`

```json
{
  "id": "re_mock_...",
  "object": "refund",
  "amount": 1000,
  "currency": "usd",
  "payment_intent": "pi_mock_...",
  "status": "succeeded",
  "created": 1700000000
}
```

---

## Behavior

### Success

- Default outcome is `succeeded` when no `metadata[scenario]` is provided.
- A succeeded payment is stored in memory and can be retrieved or refunded.
- A `payment_intent.succeeded` webhook event is dispatched asynchronously after a configurable delay (default: 2500 ms).

### Validation

| Condition | HTTP | Error code |
|---|---|---|
| `amount` ≤ 0 | 422 | `amount_too_small` |
| `amount` is not a valid integer | 422 | `invalid_amount` |
| `metadata[scenario]` is an unknown value | 422 | `invalid_scenario` |
| `X-Mock-Scenario` header is an unknown value | 422 | `invalid_latency_scenario` |
| Refund `amount` exceeds payment `amount` | 400 | `amount_too_large` |

### Latency simulation

Send the `X-Mock-Scenario` header to add artificial delay:

| Header value | Default delay |
|---|---|
| `slow` | 3000 ms |
| `timeout` | 30 000 ms |

---

## Edge Cases

| Scenario | HTTP | Error code | Message |
|---|---|---|---|
| `GET` / refund with unknown ID | 404 | `resource_missing` | `No such payment intent: {id}` |
| `metadata[scenario]=declined` | 400 | `card_declined` | `Your card was declined.` |
| `metadata[scenario]=server_error` | 500 | *(none)* | `An error occurred on our server. Please try again.` |
| Refund amount > original amount | 400 | `amount_too_large` | `Refund amount exceeds the payment amount.` |

**Error response shape**

```json
{
  "error": {
    "type": "card_error",
    "code": "card_declined",
    "message": "Your card was declined.",
    "payment_intent": {
      "id": "pi_mock_...",
      "status": "requires_payment_method"
    }
  }
}
```

The `payment_intent` field is omitted when not applicable.

---

## Limitations

- In-memory only — no database, no restart persistence
- Only succeeded payments are stored (declined/server_error payments cannot be retrieved)
- Refunding a payment does not update the stored payment status
- Webhook delivery has no retry logic; failures are logged and ignored
- No real Stripe signature verification

---

## Running Locally

**Requirements:** Java 21, Maven

```bash
./mvnw spring-boot:run
```

The service starts on **port 8085** by default.

**Key configuration** (`src/main/resources/application.properties`):

```properties
mock.webhook.target-url=http://localhost:8080/api/v1/webhooks/payment
mock.webhook.delay-ms=2500
mock.scenario.slow-delay-ms=3000
mock.scenario.timeout-delay-ms=30000
```

---

## Examples

### Create a successful payment

```bash
curl -X POST http://localhost:8085/v1/payment_intents \
  -d "amount=2000" \
  -d "currency=usd" \
  -d "payment_method=pm_card_visa" \
  -d "receipt_email=user@example.com"
```

```json
{
  "id": "pi_mock_3f2a...",
  "object": "payment_intent",
  "amount": 2000,
  "currency": "usd",
  "status": "succeeded",
  "client_secret": "pi_mock_3f2a..._secret_9b1c...",
  "payment_method": "pm_card_visa",
  "receipt_email": "user@example.com",
  "metadata": {},
  "created": 1700000000
}
```

### Simulate a declined card

```bash
curl -X POST http://localhost:8085/v1/payment_intents \
  -d "amount=2000" \
  -d "currency=usd" \
  -d "payment_method=pm_card_visa" \
  -d "metadata[scenario]=declined"
```

```json
{
  "error": {
    "type": "card_error",
    "code": "card_declined",
    "message": "Your card was declined.",
    "payment_intent": {
      "id": "pi_mock_...",
      "status": "requires_payment_method"
    }
  }
}
```

---

## Why This Project Matters

Building against a real payment provider in tests is slow, flaky, and expensive. This service demonstrates how to design a controllable test double that:

- mirrors a real API contract (Stripe-compatible shape and error structure),
- supports scenario-driven testing without mocking at the HTTP client level,
- simulates async behavior (webhooks) and failure modes (latency, declines, server errors),
- keeps the implementation minimal and easy to reason about.

It reflects practical engineering decisions: thread-safe in-memory storage, factory-based response construction, clean separation between scenario resolution and business logic, and a test suite that covers every documented behavior.
