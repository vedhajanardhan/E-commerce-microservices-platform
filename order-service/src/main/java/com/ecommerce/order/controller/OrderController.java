package com.ecommerce.order.controller;

import com.ecommerce.order.dto.request.CancelOrderRequest;
import com.ecommerce.order.dto.request.PlaceOrderRequest;
import com.ecommerce.order.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.security.CurrentUserProvider;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Orders", description = "Place orders from cart, view history/status, cancel")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;

    public OrderController(OrderService orderService, CurrentUserProvider currentUserProvider) {
        this.orderService = orderService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Place an order from the current user's cart (validates stock, charges payment, clears cart)")
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(Authentication authentication, @Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.placeOrder(currentUserProvider.getUserId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get a single order by id (must be owned by the caller, or caller must be admin)")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(Authentication authentication, @PathVariable UUID orderId) {
        UUID userId = currentUserProvider.getUserId(authentication);
        boolean isAdmin = currentUserProvider.isAdmin(authentication);
        return ResponseEntity.ok(orderService.getOrder(orderId, userId, isAdmin));
    }

    @Operation(summary = "View the current user's order history")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            Authentication authentication, @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrderHistory(currentUserProvider.getUserId(authentication), pageable));
    }

    @Operation(summary = "Cancel an order (only PENDING/CONFIRMED orders are cancellable)")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            Authentication authentication, @PathVariable UUID orderId, @Valid @RequestBody CancelOrderRequest request) {
        UUID userId = currentUserProvider.getUserId(authentication);
        boolean isAdmin = currentUserProvider.isAdmin(authentication);
        return ResponseEntity.ok(orderService.cancelOrder(orderId, userId, isAdmin, request.reason()));
    }

    @Operation(summary = "List all orders across all users, optionally filtered by status (admin only)")
    @GetMapping("/admin")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(status, pageable));
    }

    @Operation(summary = "Update an order's status, e.g. to SHIPPED or DELIVERED (admin only)")
    @PatchMapping("/admin/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request.status()));
    }
}
