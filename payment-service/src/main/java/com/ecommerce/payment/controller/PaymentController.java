package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.request.ChargeRequest;
import com.ecommerce.payment.dto.response.PaymentResponse;
import com.ecommerce.payment.dto.response.PaymentResult;
import com.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Payments", description = "Dummy payment gateway: charge, payment history")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Charge an order (called synchronously by order-service during checkout)")
    @PostMapping("/charge")
    public ResponseEntity<PaymentResult> charge(@Valid @RequestBody ChargeRequest request) {
        return ResponseEntity.ok(paymentService.charge(request));
    }

    @Operation(summary = "Get all payment attempts for an order (a retried order may have more than one)")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsForOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsForOrder(orderId));
    }

    @Operation(summary = "Look up a payment by its transaction id")
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponse> getByTransactionId(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getByTransactionId(transactionId));
    }

    @Operation(summary = "List all payments across all users (admin only)")
    @GetMapping("/admin")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }
}
