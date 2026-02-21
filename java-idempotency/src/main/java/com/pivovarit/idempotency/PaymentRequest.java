package com.pivovarit.idempotency;

public record PaymentRequest(String customerId, int amount) {}
