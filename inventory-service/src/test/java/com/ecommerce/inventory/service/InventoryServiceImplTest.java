package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.request.StockValidationRequest;
import com.ecommerce.inventory.dto.response.InventoryResponse;
import com.ecommerce.inventory.dto.response.StockValidationResponse;
import com.ecommerce.inventory.entity.InventoryItem;
import com.ecommerce.inventory.entity.ProcessedOrderEvent;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.event.OrderItemEvent;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.mapper.InventoryMapper;
import com.ecommerce.inventory.repository.InventoryItemRepository;
import com.ecommerce.inventory.repository.ProcessedOrderCancellationRepository;
import com.ecommerce.inventory.repository.ProcessedOrderEventRepository;
import com.ecommerce.inventory.repository.StockMovementRepository;
import com.ecommerce.inventory.service.impl.InventoryEventPublisher;
import com.ecommerce.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private ProcessedOrderEventRepository processedOrderEventRepository;
    @Mock private ProcessedOrderCancellationRepository processedOrderCancellationRepository;
    @Mock private InventoryMapper inventoryMapper;
    @Mock private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
    }

    @Test
    void decreaseStock_withSufficientStock_succeeds() {
        InventoryItem item = InventoryItem.builder().productId(productId).sku("SKU-1").quantityAvailable(10).build();
        when(inventoryItemRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryMapper.toInventoryResponse(any())).thenReturn(
                new InventoryResponse(productId, "SKU-1", 5, 10, false, LocalDateTime.now()));

        InventoryResponse response = inventoryService.decreaseStock(productId, 5, "test", null);

        assertEquals(5, response.quantityAvailable());
        verify(stockMovementRepository).save(any());
        verify(eventPublisher).publishInventoryUpdated(any());
    }

    @Test
    void decreaseStock_withInsufficientStock_throwsException() {
        InventoryItem item = InventoryItem.builder().productId(productId).sku("SKU-1").quantityAvailable(2).build();
        when(inventoryItemRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(item));

        assertThrows(InsufficientStockException.class, () -> inventoryService.decreaseStock(productId, 5, "test", null));
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    void decreaseStock_whenProductNotFound_throwsResourceNotFoundException() {
        when(inventoryItemRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.decreaseStock(productId, 1, "test", null));
    }

    @Test
    void validateStock_whenAllSufficient_returnsValid() {
        InventoryItem item = InventoryItem.builder().productId(productId).quantityAvailable(10).build();
        when(inventoryItemRepository.findById(productId)).thenReturn(Optional.of(item));

        var request = new StockValidationRequest(List.of(new StockValidationRequest.StockCheckItem(productId, 5)));
        StockValidationResponse response = inventoryService.validateStock(request);

        assertTrue(response.valid());
        assertTrue(response.insufficientProductIds().isEmpty());
    }

    @Test
    void validateStock_whenInsufficient_returnsInvalidWithProductId() {
        InventoryItem item = InventoryItem.builder().productId(productId).quantityAvailable(2).build();
        when(inventoryItemRepository.findById(productId)).thenReturn(Optional.of(item));

        var request = new StockValidationRequest(List.of(new StockValidationRequest.StockCheckItem(productId, 5)));
        StockValidationResponse response = inventoryService.validateStock(request);

        assertFalse(response.valid());
        assertEquals(List.of(productId), response.insufficientProductIds());
    }

    @Test
    void processOrderCreated_whenAlreadyProcessed_isNoOp() {
        UUID orderId = UUID.randomUUID();
        when(processedOrderEventRepository.existsById(orderId)).thenReturn(true);

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, UUID.randomUUID(),
                List.of(new OrderItemEvent(productId, 1)), Instant.now());

        inventoryService.processOrderCreated(event);

        verify(inventoryItemRepository, never()).findByIdForUpdate(any());
        verify(processedOrderEventRepository, never()).save(any());
    }

    @Test
    void processOrderCreated_withSufficientStock_decrementsAndMarksProcessed() {
        UUID orderId = UUID.randomUUID();
        when(processedOrderEventRepository.existsById(orderId)).thenReturn(false);

        InventoryItem item = InventoryItem.builder().productId(productId).sku("SKU-1").quantityAvailable(10).build();
        when(inventoryItemRepository.findById(productId)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryMapper.toInventoryResponse(any())).thenReturn(
                new InventoryResponse(productId, "SKU-1", 8, 10, false, LocalDateTime.now()));

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, UUID.randomUUID(),
                List.of(new OrderItemEvent(productId, 2)), Instant.now());

        inventoryService.processOrderCreated(event);

        verify(processedOrderEventRepository).save(any(ProcessedOrderEvent.class));
        verify(eventPublisher, never()).publishStockReservationFailed(any());
    }

    @Test
    void processOrderCreated_withInsufficientStock_publishesFailureAndMarksProcessed() {
        UUID orderId = UUID.randomUUID();
        when(processedOrderEventRepository.existsById(orderId)).thenReturn(false);

        InventoryItem item = InventoryItem.builder().productId(productId).sku("SKU-1").quantityAvailable(1).build();
        when(inventoryItemRepository.findById(productId)).thenReturn(Optional.of(item));

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, UUID.randomUUID(),
                List.of(new OrderItemEvent(productId, 5)), Instant.now());

        inventoryService.processOrderCreated(event);

        verify(eventPublisher).publishStockReservationFailed(any());
        verify(processedOrderEventRepository).save(any(ProcessedOrderEvent.class));
        verify(inventoryItemRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void processOrderCancelled_whenNotYetProcessed_restocksAndMarksProcessed() {
        UUID orderId = UUID.randomUUID();
        when(processedOrderCancellationRepository.existsById(orderId)).thenReturn(false);

        InventoryItem item = InventoryItem.builder().productId(productId).sku("SKU-1").quantityAvailable(5).build();
        when(inventoryItemRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryMapper.toInventoryResponse(any())).thenReturn(
                new InventoryResponse(productId, "SKU-1", 9, 10, false, LocalDateTime.now()));

        var event = new com.ecommerce.inventory.event.OrderCancelledEvent(
                orderId, List.of(new com.ecommerce.inventory.event.OrderItemEvent(productId, 4)),
                "customer request", Instant.now());

        inventoryService.processOrderCancelled(event);

        verify(inventoryItemRepository).save(any());
        verify(processedOrderCancellationRepository).save(any());
    }

    @Test
    void processOrderCancelled_whenAlreadyProcessed_isNoOp() {
        UUID orderId = UUID.randomUUID();
        when(processedOrderCancellationRepository.existsById(orderId)).thenReturn(true);

        var event = new com.ecommerce.inventory.event.OrderCancelledEvent(
                orderId, List.of(new com.ecommerce.inventory.event.OrderItemEvent(productId, 4)),
                "customer request", Instant.now());

        inventoryService.processOrderCancelled(event);

        verify(inventoryItemRepository, never()).findByIdForUpdate(any());
        verify(processedOrderCancellationRepository, never()).save(any());
    }
}
