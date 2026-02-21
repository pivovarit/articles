package com.pivovarit.idempotency;

public record PaymentResponse(String transactionId, String status) {}
