package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * order-service calls payment-service synchronously (not via Kafka)
 * because the user needs an immediate success/failure result to know
 * whether their order actually went through — this is an orchestration
 * step, not a fire-and-forget background process. Contrast with
 * inventory's stock decrement, which is fine to happen asynchronously
 * after the order is already confirmed.
 */
@FeignClient(name = "PAYMENT-SERVICE", path = "/api/payments", configuration = com.ecommerce.order.config.FeignConfig.class)
public interface PaymentServiceClient {

    @PostMapping("/charge")
    PaymentResult charge(@RequestBody ChargeRequest request);

    record ChargeRequest(UUID orderId, UUID userId, BigDecimal amount) {
    }

    record PaymentResult(String status, String transactionId, String message) {
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
    }
}
