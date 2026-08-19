package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.request.AddItemRequest;
import com.ecommerce.cart.dto.request.UpdateQuantityRequest;
import com.ecommerce.cart.dto.response.CartResponse;
import com.ecommerce.cart.security.CurrentUserProvider;
import com.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Cart", description = "Shopping cart: add, remove, update quantity, view")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CurrentUserProvider currentUserProvider;

    public CartController(CartService cartService, CurrentUserProvider currentUserProvider) {
        this.cartService = cartService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "View the current user's cart")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(currentUserProvider.getUserId(authentication)));
    }

    @Operation(summary = "Add an item to the cart (or increase quantity if it's already there)")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(Authentication authentication, @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(currentUserProvider.getUserId(authentication), request));
    }

    @Operation(summary = "Set an item's quantity directly")
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(
            Authentication authentication,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        return ResponseEntity.ok(
                cartService.updateQuantity(currentUserProvider.getUserId(authentication), productId, request.quantity()));
    }

    @Operation(summary = "Remove an item from the cart")
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(Authentication authentication, @PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.removeItem(currentUserProvider.getUserId(authentication), productId));
    }

    @Operation(summary = "Clear the entire cart (e.g. after order placement)")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(currentUserProvider.getUserId(authentication));
        return ResponseEntity.noContent().build();
    }
}
