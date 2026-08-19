package com.ecommerce.cart.service.impl;

import com.ecommerce.cart.client.InventoryClient;
import com.ecommerce.cart.client.ProductInfo;
import com.ecommerce.cart.client.ProductServiceClient;
import com.ecommerce.cart.client.StockValidationRequest;
import com.ecommerce.cart.dto.request.AddItemRequest;
import com.ecommerce.cart.dto.response.CartResponse;
import com.ecommerce.cart.exception.CartItemNotFoundException;
import com.ecommerce.cart.exception.DownstreamServiceException;
import com.ecommerce.cart.exception.InsufficientStockException;
import com.ecommerce.cart.exception.ProductNotAvailableException;
import com.ecommerce.cart.mapper.CartMapper;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.CartService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryClient inventoryClient;
    private final CartMapper cartMapper;

    public CartServiceImpl(
            CartRepository cartRepository,
            ProductServiceClient productServiceClient,
            InventoryClient inventoryClient,
            CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.productServiceClient = productServiceClient;
        this.inventoryClient = inventoryClient;
        this.cartMapper = cartMapper;
    }

    @Override
    public CartResponse getCart(UUID userId) {
        return cartMapper.toCartResponse(loadOrCreate(userId));
    }

    @Override
    public CartResponse addItem(UUID userId, AddItemRequest request) {
        Cart cart = loadOrCreate(userId);

        ProductInfo product = fetchProduct(request.productId());
        if (!product.active()) {
            throw new ProductNotAvailableException("Product '" + product.name() + "' is not currently available");
        }

        int desiredQuantity = cart.findItem(request.productId())
                .map(existing -> existing.getQuantity() + request.quantity())
                .orElse(request.quantity());

        checkStock(request.productId(), desiredQuantity);

        cart.findItem(request.productId()).ifPresentOrElse(
                existing -> existing.setQuantity(desiredQuantity),
                () -> cart.getItems().add(CartItem.builder()
                        .productId(product.id())
                        .sku(product.sku())
                        .productName(product.name())
                        .unitPrice(product.price())
                        .quantity(request.quantity())
                        .build())
        );

        return saveAndRespond(cart);
    }

    @Override
    public CartResponse updateQuantity(UUID userId, UUID productId, int quantity) {
        Cart cart = loadOrCreate(userId);
        CartItem item = cart.findItem(productId)
                .orElseThrow(() -> new CartItemNotFoundException("Product " + productId + " is not in the cart"));

        checkStock(productId, quantity);
        item.setQuantity(quantity);

        return saveAndRespond(cart);
    }

    @Override
    public CartResponse removeItem(UUID userId, UUID productId) {
        Cart cart = loadOrCreate(userId);
        cart.removeItem(productId);
        return saveAndRespond(cart);
    }

    @Override
    public void clearCart(UUID userId) {
        cartRepository.deleteByUserId(userId);
        log.info("Cart cleared for userId={}", userId);
    }

    private Cart loadOrCreate(UUID userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> Cart.empty(userId));
    }

    private CartResponse saveAndRespond(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        return cartMapper.toCartResponse(cart);
    }

    private ProductInfo fetchProduct(UUID productId) {
        try {
            return productServiceClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            throw new ProductNotAvailableException("Product not found: " + productId);
        } catch (FeignException e) {
            log.error("product-service call failed for productId={}: status={}", productId, e.status(), e);
            throw new DownstreamServiceException("Unable to reach product catalog", e);
        }
    }

    private void checkStock(UUID productId, int quantity) {
        try {
            var response = inventoryClient.validateStock(
                    new StockValidationRequest(List.of(new StockValidationRequest.StockCheckItem(productId, quantity))));
            if (!response.valid()) {
                throw new InsufficientStockException("Insufficient stock for product " + productId);
            }
        } catch (FeignException e) {
            log.error("inventory-service call failed for productId={}: status={}", productId, e.status(), e);
            throw new DownstreamServiceException("Unable to verify stock availability", e);
        }
    }
}
