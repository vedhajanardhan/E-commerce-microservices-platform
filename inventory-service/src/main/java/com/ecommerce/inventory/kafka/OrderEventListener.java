package com.ecommerce.inventory.kafka;

import com.ecommerce.inventory.event.OrderCancelledEvent;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    private final InventoryService inventoryService;

    public OrderEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Consumer group "inventory-service" — if multiple instances of this
     * service run (horizontal scaling), Kafka partitions the topic across
     * them so each order-created event is processed by exactly one
     * instance, while still allowing parallelism across different orders.
     * <p>
     * Errors bubble up to Spring Kafka's default error handling (retry
     * then send to a dead-letter topic if configured), rather than being
     * swallowed here — a stock decrement failure must not be silently lost.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service",
            containerFactory = "orderCreatedListenerContainerFactory")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, items={}", event.orderId(), event.items().size());
        inventoryService.processOrderCreated(event);
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "inventory-service",
            containerFactory = "orderCancelledListenerContainerFactory")
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent: orderId={}, items={}", event.orderId(), event.items().size());
        inventoryService.processOrderCancelled(event);
    }
}
