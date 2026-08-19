package com.ecommerce.payment.service;

import com.ecommerce.payment.service.impl.DummyPaymentGateway;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DummyPaymentGatewayTest {

    @Test
    void process_withDeclineTriggerCents_alwaysFails() {
        Random random = mock(Random.class);
        when(random.nextDouble()).thenReturn(1.0); // would never randomly fail
        DummyPaymentGateway gateway = new DummyPaymentGateway(random, 0.0);

        var result = gateway.process(new BigDecimal("49.13"));

        assertFalse(result.success());
        assertNull(result.transactionId());
        assertTrue(result.message().toLowerCase().contains("declined"));
    }

    @Test
    void process_withNormalAmount_andZeroRandomFailureRate_alwaysSucceeds() {
        Random random = mock(Random.class);
        when(random.nextDouble()).thenReturn(0.99);
        DummyPaymentGateway gateway = new DummyPaymentGateway(random, 0.0);

        var result = gateway.process(new BigDecimal("49.99"));

        assertTrue(result.success());
        assertNotNull(result.transactionId());
        assertTrue(result.transactionId().startsWith("txn_"));
    }

    @Test
    void process_whenRandomRollsBelowFailureRate_fails() {
        Random random = mock(Random.class);
        when(random.nextDouble()).thenReturn(0.01);
        DummyPaymentGateway gateway = new DummyPaymentGateway(random, 0.5);

        var result = gateway.process(new BigDecimal("20.00"));

        assertFalse(result.success());
        assertNull(result.transactionId());
    }
}
