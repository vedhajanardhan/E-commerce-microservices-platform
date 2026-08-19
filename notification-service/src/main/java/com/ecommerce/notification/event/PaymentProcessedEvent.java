package com.ecommerce.notification.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentProcessedEvent(
        UUID orderId,
        UUID userId,
        String status,
        String transactionId,
        BigDecimal amount,
        Instant timestamp
) {
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
}
