package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.response.NotificationResponse;
import com.ecommerce.notification.event.InventoryUpdatedEvent;
import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.event.PaymentProcessedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    void handleOrderCreated(OrderCreatedEvent event);

    void handlePaymentProcessed(PaymentProcessedEvent event);

    void handleInventoryUpdated(InventoryUpdatedEvent event);

    Page<NotificationResponse> getNotificationsForUser(UUID userId, Pageable pageable);

    Page<NotificationResponse> getAllNotifications(Pageable pageable);
}
