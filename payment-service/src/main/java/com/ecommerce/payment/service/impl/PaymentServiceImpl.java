package com.ecommerce.payment.service.impl;

import com.ecommerce.payment.dto.request.ChargeRequest;
import com.ecommerce.payment.dto.response.PaymentResponse;
import com.ecommerce.payment.dto.response.PaymentResult;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.event.PaymentProcessedEvent;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.mapper.PaymentMapper;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final DummyPaymentGateway paymentGateway;
    private final PaymentMapper paymentMapper;
    private final PaymentEventPublisher eventPublisher;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            DummyPaymentGateway paymentGateway,
            PaymentMapper paymentMapper,
            PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.paymentMapper = paymentMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PaymentResult charge(ChargeRequest request) {
        var gatewayResult = paymentGateway.process(request.amount());

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .userId(request.userId())
                .amount(request.amount())
                .status(gatewayResult.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .transactionId(gatewayResult.transactionId())
                .failureReason(gatewayResult.success() ? null : gatewayResult.message())
                .build();

        Payment saved = paymentRepository.save(payment);

        eventPublisher.publishPaymentProcessed(new PaymentProcessedEvent(
                saved.getOrderId(), saved.getUserId(), saved.getStatus(),
                saved.getTransactionId(), saved.getAmount(), Instant.now()));

        if (gatewayResult.success()) {
            log.info("Payment SUCCESS: orderId={}, transactionId={}, amount={}",
                    request.orderId(), saved.getTransactionId(), request.amount());
        } else {
            log.warn("Payment FAILED: orderId={}, reason={}, amount={}",
                    request.orderId(), gatewayResult.message(), request.amount());
        }

        return new PaymentResult(saved.getStatus().name(), saved.getTransactionId(), gatewayResult.message());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForOrder(UUID orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found with transaction id: " + transactionId));
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(paymentMapper::toPaymentResponse);
    }
}
