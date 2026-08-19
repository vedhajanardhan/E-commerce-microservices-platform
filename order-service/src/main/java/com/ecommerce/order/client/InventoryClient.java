package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "INVENTORY-SERVICE", path = "/api/inventory", configuration = com.ecommerce.order.config.FeignConfig.class)
public interface InventoryClient {

    @PostMapping("/validate")
    StockValidationResponse validateStock(@RequestBody StockValidationRequest request);

    record StockValidationRequest(List<StockCheckItem> items) {
        public record StockCheckItem(UUID productId, int quantity) {
        }
    }

    record StockValidationResponse(boolean valid, List<UUID> insufficientProductIds) {
    }
}
