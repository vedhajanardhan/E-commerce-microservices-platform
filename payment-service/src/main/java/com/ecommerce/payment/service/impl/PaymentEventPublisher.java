package com.ecommerce.payment.service.impl;

import com.ecommerce.payment.event.PaymentProcessedEvent;
import com.ecommerce.payment.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        kafkaTemplate.send(KafkaTopics.PAYMENT_PROCESSED, event.orderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish PaymentProcessedEvent for orderId={}", event.orderId(), ex);
                    } else {
                        log.debug("Published PaymentProcessedEvent for orderId={}, status={}", event.orderId(), event.status());
                    }
                });
    }
}
