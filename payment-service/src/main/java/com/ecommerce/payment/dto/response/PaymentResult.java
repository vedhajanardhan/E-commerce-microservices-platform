package com.ecommerce.payment.dto.response;

public record PaymentResult(
        String status,
        String transactionId,
        String message
) {
}
