package com.ecommerce.payment.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Stands in for a real payment processor (Stripe, Razorpay, etc.). Two
 * deterministic rejection rules make behavior testable/demonstrable
 * without needing real card data, plus a configurable random failure
 * rate to simulate the intermittent declines any real gateway has.
 */
@Component
public class DummyPaymentGateway {

    /** Standard test-card convention: an amount ending in .13 always "declines" — an easy, memorable way to demo a failure path. */
    private static final BigDecimal DECLINE_TRIGGER_CENTS = new BigDecimal("0.13");

    private final Random random;
    private final double randomFailureRate;

    public DummyPaymentGateway(
            Random random,
            @Value("${payment.dummy.random-failure-rate:0.05}") double randomFailureRate) {
        this.random = random;
        this.randomFailureRate = randomFailureRate;
    }

    public GatewayResult process(BigDecimal amount) {
        if (amount.remainder(BigDecimal.ONE).setScale(2, java.math.RoundingMode.HALF_UP).equals(DECLINE_TRIGGER_CENTS)) {
            return GatewayResult.failure("Card declined by issuer");
        }
        if (random.nextDouble() < randomFailureRate) {
            return GatewayResult.failure("Payment gateway timeout, please try again");
        }
        return GatewayResult.success("txn_" + UUID.randomUUID());
    }

    public record GatewayResult(boolean success, String transactionId, String message) {
        public static GatewayResult success(String transactionId) {
            return new GatewayResult(true, transactionId, "Payment processed successfully");
        }

        public static GatewayResult failure(String message) {
            return new GatewayResult(false, null, message);
        }
    }
}
