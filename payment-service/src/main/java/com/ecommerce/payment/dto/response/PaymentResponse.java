package com.ecommerce.payment.dto.response;

import com.ecommerce.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID userId,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId,
        String failureReason,
        LocalDateTime createdAt
) {
}
