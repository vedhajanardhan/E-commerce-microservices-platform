package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.request.ChargeRequest;
import com.ecommerce.payment.dto.response.PaymentResponse;
import com.ecommerce.payment.dto.response.PaymentResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResult charge(ChargeRequest request);

    List<PaymentResponse> getPaymentsForOrder(UUID orderId);

    PaymentResponse getByTransactionId(String transactionId);

    Page<PaymentResponse> getAllPayments(Pageable pageable);
}
