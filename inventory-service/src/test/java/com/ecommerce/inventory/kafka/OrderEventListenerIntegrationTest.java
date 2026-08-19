package com.ecommerce.inventory.kafka;

import com.ecommerce.inventory.entity.InventoryItem;
import com.ecommerce.inventory.event.OrderCancelledEvent;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.event.OrderItemEvent;
import com.ecommerce.inventory.repository.InventoryItemRepository;
import com.ecommerce.inventory.repository.ProcessedOrderCancellationRepository;
import com.ecommerce.inventory.repository.ProcessedOrderEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spins up an in-memory Kafka broker (no Docker/external broker needed)
 * and drives the real publish -> @KafkaListener -> decrement -> DB
 * pipeline end to end, proving the wiring actually works rather than
 * just unit-testing the service method in isolation.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.ORDER_CREATED, KafkaTopics.ORDER_CANCELLED, KafkaTopics.INVENTORY_UPDATED, KafkaTopics.STOCK_RESERVATION_FAILED})
@DirtiesContext
class OrderEventListenerIntegrationTest {

    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private InventoryItemRepository inventoryItemRepository;
    @Autowired private ProcessedOrderEventRepository processedOrderEventRepository;
    @Autowired private ProcessedOrderCancellationRepository processedOrderCancellationRepository;

    @Test
    void publishingOrderCreated_decrementsStock_andRecordsProcessedEvent() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        inventoryItemRepository.save(InventoryItem.builder()
                .productId(productId)
                .sku("KAFKA-TEST-SKU")
                .quantityAvailable(20)
                .build());

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, UUID.randomUUID(), List.of(new OrderItemEvent(productId, 3)), Instant.now());

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, orderId.toString(), event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            InventoryItem updated = inventoryItemRepository.findById(productId).orElseThrow();
            assertEquals(17, updated.getQuantityAvailable());
            assertTrue(processedOrderEventRepository.existsById(orderId));
        });
    }

    @Test
    void publishingOrderCancelled_restocksItem_andRecordsProcessedCancellation() {
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        inventoryItemRepository.save(InventoryItem.builder()
                .productId(productId)
                .sku("KAFKA-CANCEL-TEST-SKU")
                .quantityAvailable(10)
                .build());

        OrderCancelledEvent event = new OrderCancelledEvent(
                orderId, List.of(new OrderItemEvent(productId, 4)), "customer request", Instant.now());

        kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, orderId.toString(), event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            InventoryItem updated = inventoryItemRepository.findById(productId).orElseThrow();
            assertEquals(14, updated.getQuantityAvailable());
            assertTrue(processedOrderCancellationRepository.existsById(orderId));
        });
    }
}
