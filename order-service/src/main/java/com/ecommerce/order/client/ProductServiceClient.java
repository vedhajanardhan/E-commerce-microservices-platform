package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/products", configuration = com.ecommerce.order.config.FeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/{id}")
    ProductInfo getProductById(@PathVariable("id") UUID id);
}
