package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.request.CreateInventoryItemRequest;
import com.ecommerce.inventory.dto.request.StockValidationRequest;
import com.ecommerce.inventory.dto.response.InventoryResponse;
import com.ecommerce.inventory.dto.response.StockValidationResponse;
import com.ecommerce.inventory.event.OrderCancelledEvent;
import com.ecommerce.inventory.event.OrderCreatedEvent;

import java.util.UUID;

public interface InventoryService {

    InventoryResponse getInventory(UUID productId);

    InventoryResponse createInventoryItem(CreateInventoryItemRequest request);

    InventoryResponse increaseStock(UUID productId, int quantity, String reason, UUID orderId);

    InventoryResponse decreaseStock(UUID productId, int quantity, String reason, UUID orderId);

    StockValidationResponse validateStock(StockValidationRequest request);

    /** Entry point for the Kafka listener — idempotently decrements stock for every item in the order. */
    void processOrderCreated(OrderCreatedEvent event);

    /** Entry point for the Kafka listener — idempotently restocks every item in a cancelled order. */
    void processOrderCancelled(OrderCancelledEvent event);
}
