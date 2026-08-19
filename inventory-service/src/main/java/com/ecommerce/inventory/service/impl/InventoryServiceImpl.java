package com.ecommerce.inventory.service.impl;

import com.ecommerce.inventory.dto.request.CreateInventoryItemRequest;
import com.ecommerce.inventory.dto.request.StockValidationRequest;
import com.ecommerce.inventory.dto.response.InventoryResponse;
import com.ecommerce.inventory.dto.response.StockValidationResponse;
import com.ecommerce.inventory.entity.InventoryItem;
import com.ecommerce.inventory.entity.MovementType;
import com.ecommerce.inventory.entity.ProcessedOrderCancellation;
import com.ecommerce.inventory.entity.ProcessedOrderEvent;
import com.ecommerce.inventory.entity.StockMovement;
import com.ecommerce.inventory.event.InventoryUpdatedEvent;
import com.ecommerce.inventory.event.OrderCancelledEvent;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.event.OrderItemEvent;
import com.ecommerce.inventory.event.StockReservationFailedEvent;
import com.ecommerce.inventory.exception.DuplicateInventoryItemException;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.mapper.InventoryMapper;
import com.ecommerce.inventory.repository.InventoryItemRepository;
import com.ecommerce.inventory.repository.ProcessedOrderCancellationRepository;
import com.ecommerce.inventory.repository.ProcessedOrderEventRepository;
import com.ecommerce.inventory.repository.StockMovementRepository;
import com.ecommerce.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProcessedOrderEventRepository processedOrderEventRepository;
    private final ProcessedOrderCancellationRepository processedOrderCancellationRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryEventPublisher eventPublisher;

    public InventoryServiceImpl(
            InventoryItemRepository inventoryItemRepository,
            StockMovementRepository stockMovementRepository,
            ProcessedOrderEventRepository processedOrderEventRepository,
            ProcessedOrderCancellationRepository processedOrderCancellationRepository,
            InventoryMapper inventoryMapper,
            InventoryEventPublisher eventPublisher) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.processedOrderEventRepository = processedOrderEventRepository;
        this.processedOrderCancellationRepository = processedOrderCancellationRepository;
        this.inventoryMapper = inventoryMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(UUID productId) {
        InventoryItem item = inventoryItemRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No inventory record for product: " + productId
                        ));

        return inventoryMapper.toInventoryResponse(item);
    }

    @Override
    public InventoryResponse createInventoryItem(CreateInventoryItemRequest request) {

        if (inventoryItemRepository.existsById(request.productId())) {
            throw new DuplicateInventoryItemException(
                    "Inventory already provisioned for product: " + request.productId()
            );
        }

        InventoryItem item = InventoryItem.builder()
                .productId(request.productId())
                .sku(request.sku())
                .quantityAvailable(request.initialQuantity())
                .reorderThreshold(
                        request.reorderThreshold() != null
                                ? request.reorderThreshold()
                                : 10
                )
                .build();

        InventoryItem saved = inventoryItemRepository.save(item);

        log.info(
                "Inventory item provisioned: productId={}, initialQuantity={}",
                saved.getProductId(),
                saved.getQuantityAvailable()
        );

        return inventoryMapper.toInventoryResponse(saved);
    }

    @Override
    public InventoryResponse increaseStock(
            UUID productId,
            int quantity,
            String reason,
            UUID orderId) {

        InventoryItem item = lockAndGet(productId);

        item.setQuantityAvailable(
                item.getQuantityAvailable() + quantity
        );

        InventoryItem saved = inventoryItemRepository.save(item);

        recordMovement(
                productId,
                MovementType.INCREASE,
                quantity,
                saved.getQuantityAvailable(),
                reason,
                orderId
        );

        publishUpdate(
                saved,
                MovementType.INCREASE,
                quantity
        );

        log.info(
                "Stock increased: productId={}, by={}, newQuantity={}",
                productId,
                quantity,
                saved.getQuantityAvailable()
        );

        return inventoryMapper.toInventoryResponse(saved);
    }

    @Override
    public InventoryResponse decreaseStock(
            UUID productId,
            int quantity,
            String reason,
            UUID orderId) {

        InventoryItem item = lockAndGet(productId);

        if (item.getQuantityAvailable() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + productId
                            + ": requested " + quantity
                            + " but only "
                            + item.getQuantityAvailable()
                            + " available"
            );
        }

        item.setQuantityAvailable(
                item.getQuantityAvailable() - quantity
        );

        InventoryItem saved = inventoryItemRepository.save(item);

        recordMovement(
                productId,
                MovementType.DECREASE,
                quantity,
                saved.getQuantityAvailable(),
                reason,
                orderId
        );

        publishUpdate(
                saved,
                MovementType.DECREASE,
                quantity
        );

        log.info(
                "Stock decreased: productId={}, by={}, newQuantity={}",
                productId,
                quantity,
                saved.getQuantityAvailable()
        );

        return inventoryMapper.toInventoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StockValidationResponse validateStock(
            StockValidationRequest request) {

        List<UUID> insufficient = new ArrayList<>();

        for (var checkItem : request.items()) {

            InventoryItem item =
                    inventoryItemRepository
                            .findById(checkItem.productId())
                            .orElse(null);

            if (item == null ||
                    item.getQuantityAvailable() < checkItem.quantity()) {

                insufficient.add(checkItem.productId());
            }
        }

        // IMPORTANT:
        // valid() was changed to success()
        return insufficient.isEmpty()
                ? StockValidationResponse.success()
                : StockValidationResponse.invalid(insufficient);
    }

    @Override
    public void processOrderCreated(OrderCreatedEvent event) {

        if (processedOrderEventRepository.existsById(event.orderId())) {

            log.info(
                    "OrderCreatedEvent for orderId={} already processed, skipping (duplicate delivery)",
                    event.orderId()
            );

            return;
        }

        List<UUID> insufficientProducts = new ArrayList<>();

        /*
         * First pass:
         * Verify every item can actually be fulfilled before
         * mutating any stock.
         */
        for (OrderItemEvent orderItem : event.items()) {

            InventoryItem item =
                    inventoryItemRepository
                            .findById(orderItem.productId())
                            .orElse(null);

            if (item == null ||
                    item.getQuantityAvailable() < orderItem.quantity()) {

                insufficientProducts.add(orderItem.productId());
            }
        }

        if (!insufficientProducts.isEmpty()) {

            eventPublisher.publishStockReservationFailed(
                    new StockReservationFailedEvent(
                            event.orderId(),
                            insufficientProducts,
                            "Insufficient stock at fulfillment time",
                            Instant.now()
                    )
            );

            processedOrderEventRepository.save(
                    new ProcessedOrderEvent(event.orderId())
            );

            return;
        }

        for (OrderItemEvent orderItem : event.items()) {

            decreaseStock(
                    orderItem.productId(),
                    orderItem.quantity(),
                    "Order fulfillment",
                    event.orderId()
            );
        }

        processedOrderEventRepository.save(
                new ProcessedOrderEvent(event.orderId())
        );

        log.info(
                "Order {} processed successfully, stock decremented for {} item(s)",
                event.orderId(),
                event.items().size()
        );
    }

    @Override
    public void processOrderCancelled(OrderCancelledEvent event) {

        if (processedOrderCancellationRepository.existsById(event.orderId())) {

            log.info(
                    "OrderCancelledEvent for orderId={} already processed, skipping (duplicate delivery)",
                    event.orderId()
            );

            return;
        }

        for (OrderItemEvent item : event.items()) {

            try {

                increaseStock(
                        item.productId(),
                        item.quantity(),
                        "Order cancellation: " + event.reason(),
                        event.orderId()
                );

            } catch (ResourceNotFoundException e) {

                log.warn(
                        "Could not restock productId={} for cancelled orderId={}: {}",
                        item.productId(),
                        event.orderId(),
                        e.getMessage()
                );
            }
        }

        processedOrderCancellationRepository.save(
                new ProcessedOrderCancellation(event.orderId())
        );

        log.info(
                "Order {} cancellation processed, stock restored for {} item(s)",
                event.orderId(),
                event.items().size()
        );
    }

    private InventoryItem lockAndGet(UUID productId) {

        return inventoryItemRepository
                .findByIdForUpdate(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No inventory record for product: " + productId
                        ));
    }

    private void recordMovement(
            UUID productId,
            MovementType type,
            int quantity,
            int resultingQuantity,
            String reason,
            UUID orderId) {

        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .movementType(type)
                .quantity(quantity)
                .resultingQuantity(resultingQuantity)
                .reason(reason)
                .orderId(orderId)
                .build();

        stockMovementRepository.save(movement);
    }

    private void publishUpdate(
            InventoryItem item,
            MovementType type,
            int quantityChanged) {

        eventPublisher.publishInventoryUpdated(
                new InventoryUpdatedEvent(
                        item.getProductId(),
                        type,
                        quantityChanged,
                        item.getQuantityAvailable(),
                        item.isLowStock(),
                        Instant.now()
                )
        );
    }
}