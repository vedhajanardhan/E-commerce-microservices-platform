package com.ecommerce.inventory.service.impl;

import com.ecommerce.inventory.event.InventoryUpdatedEvent;
import com.ecommerce.inventory.event.StockReservationFailedEvent;
import com.ecommerce.inventory.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryUpdated(InventoryUpdatedEvent event) {
        // Keyed by productId so all updates for the same product land on
        // the same partition and are processed in order by any consumer.
        kafkaTemplate.send(KafkaTopics.INVENTORY_UPDATED, event.productId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish InventoryUpdatedEvent for productId={}", event.productId(), ex);
                    } else {
                        log.debug("Published InventoryUpdatedEvent for productId={}", event.productId());
                    }
                });
    }

    public void publishStockReservationFailed(StockReservationFailedEvent event) {
        kafkaTemplate.send(KafkaTopics.STOCK_RESERVATION_FAILED, event.orderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish StockReservationFailedEvent for orderId={}", event.orderId(), ex);
                    } else {
                        log.warn("Published StockReservationFailedEvent for orderId={}, insufficientProducts={}",
                                event.orderId(), event.insufficientProductIds());
                    }
                });
    }
}
