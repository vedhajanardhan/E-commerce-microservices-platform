package com.ecommerce.notification.kafka;

import com.ecommerce.notification.event.InventoryUpdatedEvent;
import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.event.PaymentProcessedEvent;
import com.ecommerce.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "notification-service",
            containerFactory = "orderCreatedListenerContainerFactory")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}", event.orderId());
        notificationService.handleOrderCreated(event);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED, groupId = "notification-service",
            containerFactory = "paymentProcessedListenerContainerFactory")
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent: orderId={}, status={}", event.orderId(), event.status());
        notificationService.handlePaymentProcessed(event);
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_UPDATED, groupId = "notification-service",
            containerFactory = "inventoryUpdatedListenerContainerFactory")
    public void onInventoryUpdated(InventoryUpdatedEvent event) {
        if (event.lowStock()) {
            log.info("Received low-stock InventoryUpdatedEvent: productId={}, remaining={}",
                    event.productId(), event.quantityRemaining());
        }
        notificationService.handleInventoryUpdated(event);
    }
}
