package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.request.CreateInventoryItemRequest;
import com.ecommerce.inventory.dto.request.StockAdjustmentRequest;
import com.ecommerce.inventory.dto.request.StockValidationRequest;
import com.ecommerce.inventory.dto.response.InventoryResponse;
import com.ecommerce.inventory.dto.response.StockValidationResponse;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Inventory", description = "Stock management: lookups, admin adjustments, and checkout-time validation")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Get current stock level for a product")
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @Operation(summary = "Provision an inventory record for a new product (admin only)")
    @PostMapping
    public ResponseEntity<InventoryResponse> createInventoryItem(@Valid @RequestBody CreateInventoryItemRequest request) {
        InventoryResponse response = inventoryService.createInventoryItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Manually increase stock, e.g. after a restock delivery (admin only)")
    @PostMapping("/{productId}/increase")
    public ResponseEntity<InventoryResponse> increase(
            @PathVariable UUID productId, @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(
                inventoryService.increaseStock(productId, request.quantity(), request.reason(), null));
    }

    @Operation(summary = "Manually decrease stock, e.g. for damaged/lost inventory (admin only)")
    @PostMapping("/{productId}/decrease")
    public ResponseEntity<InventoryResponse> decrease(
            @PathVariable UUID productId, @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(
                inventoryService.decreaseStock(productId, request.quantity(), request.reason(), null));
    }

    @Operation(summary = "Validate whether a set of products/quantities can be fulfilled (used by cart/order at checkout)")
    @PostMapping("/validate")
    public ResponseEntity<StockValidationResponse> validate(@Valid @RequestBody StockValidationRequest request) {
        return ResponseEntity.ok(inventoryService.validateStock(request));
    }
}
