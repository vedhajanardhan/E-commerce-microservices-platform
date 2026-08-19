package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.request.ChargeRequest;
import com.ecommerce.payment.dto.response.PaymentResponse;
import com.ecommerce.payment.dto.response.PaymentResult;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.mapper.PaymentMapper;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.impl.DummyPaymentGateway;
import com.ecommerce.payment.service.impl.PaymentEventPublisher;
import com.ecommerce.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private DummyPaymentGateway paymentGateway;
    @Mock private PaymentMapper paymentMapper;
    @Mock private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID orderId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void charge_whenGatewaySucceeds_savesSuccessPaymentAndPublishesEvent() {
        ChargeRequest request = new ChargeRequest(orderId, userId, new BigDecimal("100.00"));
        when(paymentGateway.process(request.amount())).thenReturn(
                DummyPaymentGateway.GatewayResult.success("txn_abc123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResult result = paymentService.charge(request);

        assertEquals("SUCCESS", result.status());
        assertEquals("txn_abc123", result.transactionId());
        verify(eventPublisher).publishPaymentProcessed(any());

        var captor = org.mockito.ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(PaymentStatus.SUCCESS, captor.getValue().getStatus());
    }

    @Test
    void charge_whenGatewayFails_savesFailedPaymentWithReason() {
        ChargeRequest request = new ChargeRequest(orderId, userId, new BigDecimal("49.13"));
        when(paymentGateway.process(request.amount())).thenReturn(
                DummyPaymentGateway.GatewayResult.failure("Card declined by issuer"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResult result = paymentService.charge(request);

        assertEquals("FAILED", result.status());
        assertNull(result.transactionId());

        var captor = org.mockito.ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(PaymentStatus.FAILED, captor.getValue().getStatus());
        assertEquals("Card declined by issuer", captor.getValue().getFailureReason());
        verify(eventPublisher).publishPaymentProcessed(any());
    }

    @Test
    void getByTransactionId_whenNotFound_throwsPaymentNotFoundException() {
        when(paymentRepository.findByTransactionId("nope")).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getByTransactionId("nope"));
    }

    @Test
    void getPaymentsForOrder_returnsAllAttemptsForThatOrder() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).orderId(orderId).userId(userId)
                .amount(BigDecimal.TEN).status(PaymentStatus.SUCCESS).transactionId("txn_1")
                .createdAt(LocalDateTime.now()).build();
        when(paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)).thenReturn(List.of(payment));
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(
                new PaymentResponse(payment.getId(), orderId, userId, BigDecimal.TEN,
                        PaymentStatus.SUCCESS, "txn_1", null, LocalDateTime.now()));

        List<PaymentResponse> results = paymentService.getPaymentsForOrder(orderId);

        assertEquals(1, results.size());
        assertEquals("txn_1", results.get(0).transactionId());
    }
}
