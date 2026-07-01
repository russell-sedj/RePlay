package com.replay.payment;

public record PaymentResponse(
        boolean success,
        String transactionId,
        Long orderId
) {}
