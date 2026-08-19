package com.ecommerce.notification.service;

import com.ecommerce.notification.client.AuthServiceClient;
import com.ecommerce.notification.client.AuthUserInfo;
import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.entity.NotificationType;
import com.ecommerce.notification.event.InventoryUpdatedEvent;
import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.event.OrderItemEvent;
import com.ecommerce.notification.event.PaymentProcessedEvent;
import com.ecommerce.notification.mapper.NotificationMapper;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.notification.service.impl.NotificationServiceImpl;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private EmailSender emailSender;
    @Mock private NotificationMapper notificationMapper;

    private NotificationServiceImpl notificationService;

    private UUID userId;
    private AuthUserInfo user;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository, authServiceClient, emailSender, notificationMapper, "admin@test.local");
        userId = UUID.randomUUID();
        user = new AuthUserInfo(userId, "vedha", "vedha@example.com", "Vedha", "J");
    }

    @Test
    void handleOrderCreated_sendsEmailAndPersistsNotification() {
        UUID orderId = UUID.randomUUID();
        when(authServiceClient.getUserById(userId)).thenReturn(user);
        when(emailSender.send(anyString(), anyString(), anyString())).thenReturn(true);

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, List.of(new OrderItemEvent(UUID.randomUUID(), 2)), Instant.now());
        notificationService.handleOrderCreated(event);

        verify(emailSender).send(eq("vedha@example.com"), anyString(), anyString());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationType.ORDER_CONFIRMATION, captor.getValue().getType());
        assertEquals(orderId, captor.getValue().getRelatedEntityId());
    }

    @Test
    void handleOrderCreated_whenUserLookupFails_skipsWithoutThrowing() {
        UUID orderId = UUID.randomUUID();
        Request dummyRequest = Request.create(Request.HttpMethod.GET, "/api/auth/internal/users/" + userId,
                Map.of(), null, new RequestTemplate());
        when(authServiceClient.getUserById(userId)).thenThrow(
                new FeignException.NotFound("not found", dummyRequest, null, null));

        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, List.of(new OrderItemEvent(UUID.randomUUID(), 1)), Instant.now());

        assertDoesNotThrow(() -> notificationService.handleOrderCreated(event));
        verify(emailSender, never()).send(any(), any(), any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void handlePaymentProcessed_success_sendsSuccessEmail() {
        UUID orderId = UUID.randomUUID();
        when(authServiceClient.getUserById(userId)).thenReturn(user);
        when(emailSender.send(anyString(), anyString(), anyString())).thenReturn(true);

        PaymentProcessedEvent event = new PaymentProcessedEvent(
                orderId, userId, "SUCCESS", "txn_123", new BigDecimal("99.99"), Instant.now());
        notificationService.handlePaymentProcessed(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationType.PAYMENT_SUCCESS, captor.getValue().getType());
    }

    @Test
    void handlePaymentProcessed_failure_sendsFailureEmail() {
        UUID orderId = UUID.randomUUID();
        when(authServiceClient.getUserById(userId)).thenReturn(user);
        when(emailSender.send(anyString(), anyString(), anyString())).thenReturn(true);

        PaymentProcessedEvent event = new PaymentProcessedEvent(
                orderId, userId, "FAILED", null, new BigDecimal("99.99"), Instant.now());
        notificationService.handlePaymentProcessed(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationType.PAYMENT_FAILURE, captor.getValue().getType());
    }

    @Test
    void handleInventoryUpdated_whenLowStock_sendsAdminAlert() {
        when(emailSender.send(anyString(), anyString(), anyString())).thenReturn(true);
        UUID productId = UUID.randomUUID();

        InventoryUpdatedEvent event = new InventoryUpdatedEvent(productId, "DECREASE", 2, 3, true, Instant.now());
        notificationService.handleInventoryUpdated(event);

        verify(emailSender).send(eq("admin@test.local"), anyString(), anyString());
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationType.LOW_STOCK_ALERT, captor.getValue().getType());
        assertNull(captor.getValue().getUserId());
    }

    @Test
    void handleInventoryUpdated_whenNotLowStock_doesNothing() {
        InventoryUpdatedEvent event = new InventoryUpdatedEvent(UUID.randomUUID(), "DECREASE", 2, 50, false, Instant.now());

        notificationService.handleInventoryUpdated(event);

        verify(emailSender, never()).send(any(), any(), any());
        verify(notificationRepository, never()).save(any());
    }
}
