package com.ecommerce.order.service.impl;

import com.ecommerce.order.client.CartServiceClient;
import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.PaymentServiceClient;
import com.ecommerce.order.client.ProductInfo;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.request.PlaceOrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.event.OrderCancelledEvent;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.event.OrderItemEvent;
import com.ecommerce.order.event.StockReservationFailedEvent;
import com.ecommerce.order.exception.*;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final InventoryClient inventoryClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher eventPublisher;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartServiceClient cartServiceClient,
            ProductServiceClient productServiceClient,
            InventoryClient inventoryClient,
            PaymentServiceClient paymentServiceClient,
            OrderMapper orderMapper,
            OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.cartServiceClient = cartServiceClient;
        this.productServiceClient = productServiceClient;
        this.inventoryClient = inventoryClient;
        this.paymentServiceClient = paymentServiceClient;
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OrderResponse placeOrder(UUID userId, PlaceOrderRequest request) {
        var cart = fetchCart();
        if (cart.items().isEmpty()) {
            throw new EmptyCartException("Cannot place an order from an empty cart");
        }

        // Re-price every line against product-service's current price
        // rather than trusting the cart's denormalized (possibly stale)
        // price — the cart is a UX convenience, product-service is the
        // pricing source of truth at the moment of purchase.
        List<OrderItem> orderItems = cart.items().stream()
                .map(cartItem -> {
                    ProductInfo product = fetchProduct(cartItem.productId());
                    if (!product.active()) {
                        throw new EmptyCartException("Product '" + product.name() + "' is no longer available; please remove it from your cart");
                    }
                    return OrderItem.builder()
                            .productId(product.id())
                            .sku(product.sku())
                            .productName(product.name())
                            .unitPrice(product.price())
                            .quantity(cartItem.quantity())
                            .build();
                })
                .toList();

        checkStock(orderItems);

        BigDecimal total = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .shippingAddress(request.shippingAddress())
                .build();
        orderItems.forEach(order::addItem);
        order = orderRepository.save(order);

        // Synchronous orchestration step: the shopper needs to know right
        // now whether their payment went through, unlike the inventory
        // decrement (which is fine to happen in the background after
        // this response is already sent).
        var paymentResult = charge(order.getId(), userId, total);

        if (paymentResult.isSuccess()) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentTransactionId(paymentResult.transactionId());
            order = orderRepository.save(order);

            eventPublisher.publishOrderCreated(new OrderCreatedEvent(
                    order.getId(), userId, toItemEvents(orderItems), Instant.now()));

            clearCartBestEffort();
            log.info("Order placed and confirmed: orderId={}, userId={}, total={}", order.getId(), userId, total);
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order = orderRepository.save(order);
            log.warn("Order payment failed: orderId={}, userId={}, reason={}", order.getId(), userId, paymentResult.message());
        }

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID requesterUserId, boolean isAdmin) {
        Order order = findOrderForRequester(orderId, requesterUserId, isAdmin);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrderHistory(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus statusFilter, Pageable pageable) {
        Page<Order> orders = statusFilter != null
                ? orderRepository.findByStatus(statusFilter, pageable)
                : orderRepository.findAll(pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId, UUID requesterUserId, boolean isAdmin, String reason) {
        Order order = findOrderForRequester(orderId, requesterUserId, isAdmin);

        if (!order.isCancellable()) {
            throw new OrderNotCancellableException(
                    "Order " + orderId + " cannot be cancelled from its current status: " + order.getStatus());
        }

        boolean wasConfirmed = order.getStatus() == OrderStatus.CONFIRMED;
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        Order saved = orderRepository.save(order);

        if (wasConfirmed) {
            // Only a CONFIRMED order ever had stock decremented (via the
            // order-created event), so only that case needs restocking.
            eventPublisher.publishOrderCancelled(new OrderCancelledEvent(
                    saved.getId(), toItemEvents(saved.getItems()), reason, Instant.now()));
        }

        log.info("Order cancelled: orderId={}, reason={}", orderId, reason);
        return orderMapper.toOrderResponse(saved);
    }

    @Override
    public OrderResponse updateStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Order status updated by admin: orderId={}, newStatus={}", orderId, newStatus);
        return orderMapper.toOrderResponse(saved);
    }

    @Override
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null) {
            log.warn("Received StockReservationFailedEvent for unknown orderId={}", event.orderId());
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.info("Order {} already cancelled, ignoring duplicate StockReservationFailedEvent", event.orderId());
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason("Stock could not be reserved: " + event.reason());
        orderRepository.save(order);
        log.warn("Order {} auto-cancelled due to failed stock reservation: insufficientProducts={}",
                event.orderId(), event.insufficientProductIds());
    }

    private Order findOrderForRequester(UUID orderId, UUID requesterUserId, boolean isAdmin) {
        if (isAdmin) {
            return orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        }
        // Deliberately the same "not found" message whether the order
        // doesn't exist or belongs to someone else — doesn't leak which
        // case it is to an unauthorized caller.
        return orderRepository.findByIdAndUserId(orderId, requesterUserId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    private CartServiceClient.CartResponse fetchCart() {
        try {
            return cartServiceClient.getCart();
        } catch (FeignException e) {
            log.error("cart-service call failed: status={}", e.status(), e);
            throw new DownstreamServiceException("Unable to retrieve your cart", e);
        }
    }

    private ProductInfo fetchProduct(UUID productId) {
        try {
            return productServiceClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            throw new EmptyCartException("A product in your cart no longer exists: " + productId);
        } catch (FeignException e) {
            log.error("product-service call failed for productId={}: status={}", productId, e.status(), e);
            throw new DownstreamServiceException("Unable to verify product details", e);
        }
    }

    private void checkStock(List<OrderItem> orderItems) {
        var items = orderItems.stream()
                .map(i -> new InventoryClient.StockValidationRequest.StockCheckItem(i.getProductId(), i.getQuantity()))
                .toList();
        try {
            var response = inventoryClient.validateStock(new InventoryClient.StockValidationRequest(items));
            if (!response.valid()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product(s): " + response.insufficientProductIds());
            }
        } catch (FeignException e) {
            log.error("inventory-service call failed during checkout: status={}", e.status(), e);
            throw new DownstreamServiceException("Unable to verify stock availability", e);
        }
    }

    private PaymentServiceClient.PaymentResult charge(UUID orderId, UUID userId, BigDecimal amount) {
        try {
            return paymentServiceClient.charge(new PaymentServiceClient.ChargeRequest(orderId, userId, amount));
        } catch (FeignException e) {
            log.error("payment-service call failed for orderId={}: status={}", orderId, e.status(), e);
            // Treat a downstream payment-service outage as a failed
            // charge (fail closed) rather than silently confirming an
            // order nobody actually paid for.
            return new PaymentServiceClient.PaymentResult("FAILED", null, "Payment service unavailable");
        }
    }

    private void clearCartBestEffort() {
        try {
            cartServiceClient.clearCart();
        } catch (FeignException e) {
            // The order is already confirmed at this point — a failure
            // to clear the cart is a UX annoyance (stale items linger),
            // not a reason to fail an otherwise-successful order.
            log.warn("Failed to clear cart after order placement (non-fatal): status={}", e.status());
        }
    }

    private List<OrderItemEvent> toItemEvents(List<OrderItem> items) {
        return items.stream().map(i -> new OrderItemEvent(i.getProductId(), i.getQuantity())).toList();
    }
}
