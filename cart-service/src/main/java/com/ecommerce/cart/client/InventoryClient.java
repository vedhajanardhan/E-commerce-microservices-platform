package com.ecommerce.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "INVENTORY-SERVICE", path = "/api/inventory", configuration = com.ecommerce.cart.config.FeignConfig.class)
public interface InventoryClient {

    @PostMapping("/validate")
    StockValidationResponse validateStock(@RequestBody StockValidationRequest request);
}
