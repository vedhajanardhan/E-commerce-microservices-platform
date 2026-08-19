package com.ecommerce.payment.event;

import com.ecommerce.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentProcessedEvent(
        UUID orderId,
        UUID userId,
        PaymentStatus status,
        String transactionId,
        BigDecimal amount,
        Instant timestamp
) {
}
