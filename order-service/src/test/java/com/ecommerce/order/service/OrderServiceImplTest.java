package com.ecommerce.order.service;

import com.ecommerce.order.client.CartServiceClient;
import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.PaymentServiceClient;
import com.ecommerce.order.client.ProductInfo;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.request.PlaceOrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.event.StockReservationFailedEvent;
import com.ecommerce.order.exception.EmptyCartException;
import com.ecommerce.order.exception.OrderNotCancellableException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.mapper.OrderMapperImpl;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.impl.OrderEventPublisher;
import com.ecommerce.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartServiceClient cartServiceClient;
    @Mock private ProductServiceClient productServiceClient;
    @Mock private InventoryClient inventoryClient;
    @Mock private PaymentServiceClient paymentServiceClient;
    @Mock private OrderEventPublisher eventPublisher;

    private final OrderMapper orderMapper = new OrderMapperImpl();

    private OrderServiceImpl orderService;
    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, cartServiceClient, productServiceClient,
                inventoryClient, paymentServiceClient, orderMapper, eventPublisher);
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    private CartServiceClient.CartResponse cartWithOneItem() {
        var item = new CartServiceClient.CartItemResponse(productId, "SKU-1", "Item", new BigDecimal("50.00"), 2, new BigDecimal("100.00"));
        return new CartServiceClient.CartResponse(userId, List.of(item), 2, new BigDecimal("100.00"), null);
    }

    @Test
    void placeOrder_withEmptyCart_throwsEmptyCartException() {
        when(cartServiceClient.getCart()).thenReturn(new CartServiceClient.CartResponse(userId, List.of(), 0, BigDecimal.ZERO, null));

        assertThrows(EmptyCartException.class, () -> orderService.placeOrder(userId, new PlaceOrderRequest("123 Main St")));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_successfulPayment_confirmsOrderAndPublishesEvent() {
        when(cartServiceClient.getCart()).thenReturn(cartWithOneItem());
        when(productServiceClient.getProductById(productId)).thenReturn(
                new ProductInfo(productId, "SKU-1", "Item", new BigDecimal("50.00"), true));
        when(inventoryClient.validateStock(any())).thenReturn(new InventoryClient.StockValidationResponse(true, List.of()));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) {
                // simulate DB assigning an id on first save
                o.setId(UUID.randomUUID());
            }
            return o;
        });
        when(paymentServiceClient.charge(any())).thenReturn(
                new PaymentServiceClient.PaymentResult("SUCCESS", "txn-123", "ok"));

        OrderResponse response = orderService.placeOrder(userId, new PlaceOrderRequest("123 Main St"));

        assertEquals(OrderStatus.CONFIRMED, response.status());
        assertEquals("txn-123", response.paymentTransactionId());
        verify(eventPublisher).publishOrderCreated(any());
        verify(cartServiceClient).clearCart();
    }

    @Test
    void placeOrder_failedPayment_marksOrderPaymentFailedAndDoesNotPublish() {
        when(cartServiceClient.getCart()).thenReturn(cartWithOneItem());
        when(productServiceClient.getProductById(productId)).thenReturn(
                new ProductInfo(productId, "SKU-1", "Item", new BigDecimal("50.00"), true));
        when(inventoryClient.validateStock(any())).thenReturn(new InventoryClient.StockValidationResponse(true, List.of()));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            if (o.getId() == null) o.setId(UUID.randomUUID());
            return o;
        });
        when(paymentServiceClient.charge(any())).thenReturn(
                new PaymentServiceClient.PaymentResult("FAILED", null, "card declined"));

        OrderResponse response = orderService.placeOrder(userId, new PlaceOrderRequest("123 Main St"));

        assertEquals(OrderStatus.PAYMENT_FAILED, response.status());
        verify(eventPublisher, never()).publishOrderCreated(any());
        verify(cartServiceClient, never()).clearCart();
    }

    @Test
    void getOrder_asNonOwner_throwsOrderNotFoundException() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(orderId, userId, false));
    }

    @Test
    void cancelOrder_whenConfirmed_publishesOrderCancelledEvent() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).userId(userId).status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("100.00")).build();
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder(orderId, userId, false, "changed my mind");

        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(eventPublisher).publishOrderCancelled(any());
    }

    @Test
    void cancelOrder_whenAlreadyShipped_throwsOrderNotCancellableException() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).userId(userId).status(OrderStatus.SHIPPED)
                .totalAmount(new BigDecimal("100.00")).build();
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        assertThrows(OrderNotCancellableException.class, () -> orderService.cancelOrder(orderId, userId, false, "too late"));
        verify(eventPublisher, never()).publishOrderCancelled(any());
    }

    @Test
    void handleStockReservationFailed_cancelsOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).userId(userId).status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("100.00")).build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.handleStockReservationFailed(new StockReservationFailedEvent(
                orderId, List.of(productId), "out of stock", Instant.now()));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void handleStockReservationFailed_whenAlreadyCancelled_isNoOp() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).userId(userId).status(OrderStatus.CANCELLED)
                .totalAmount(new BigDecimal("100.00")).build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.handleStockReservationFailed(new StockReservationFailedEvent(
                orderId, List.of(productId), "out of stock", Instant.now()));

        verify(orderRepository, never()).save(any());
    }
}
