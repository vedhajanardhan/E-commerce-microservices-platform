package com.ecommerce.order.service;

import com.ecommerce.order.dto.request.PlaceOrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.event.StockReservationFailedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse placeOrder(UUID userId, PlaceOrderRequest request);

    OrderResponse getOrder(UUID orderId, UUID requesterUserId, boolean isAdmin);

    Page<OrderResponse> getOrderHistory(UUID userId, Pageable pageable);

    Page<OrderResponse> getAllOrders(OrderStatus statusFilter, Pageable pageable);

    OrderResponse cancelOrder(UUID orderId, UUID requesterUserId, boolean isAdmin, String reason);

    OrderResponse updateStatus(UUID orderId, OrderStatus newStatus);

    /** Entry point for the Kafka listener consuming inventory-service's async stock-reservation-failed signal. */
    void handleStockReservationFailed(StockReservationFailedEvent event);
}
