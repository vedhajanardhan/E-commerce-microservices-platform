package com.ecommerce.notification.service.impl;

import com.ecommerce.notification.client.AuthServiceClient;
import com.ecommerce.notification.client.AuthUserInfo;
import com.ecommerce.notification.dto.response.NotificationResponse;
import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.entity.NotificationStatus;
import com.ecommerce.notification.entity.NotificationType;
import com.ecommerce.notification.event.InventoryUpdatedEvent;
import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.event.PaymentProcessedEvent;
import com.ecommerce.notification.mapper.NotificationMapper;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.notification.service.EmailSender;
import com.ecommerce.notification.service.NotificationService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthServiceClient authServiceClient;
    private final EmailSender emailSender;
    private final NotificationMapper notificationMapper;
    private final String adminAlertEmail;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            AuthServiceClient authServiceClient,
            EmailSender emailSender,
            NotificationMapper notificationMapper,
            @Value("${notification.admin-alert-email:admin@ecommerce-platform.local}") String adminAlertEmail) {
        this.notificationRepository = notificationRepository;
        this.authServiceClient = authServiceClient;
        this.emailSender = emailSender;
        this.notificationMapper = notificationMapper;
        this.adminAlertEmail = adminAlertEmail;
    }

    @Override
    public void handleOrderCreated(OrderCreatedEvent event) {
        AuthUserInfo user = fetchUser(event.userId());
        if (user == null) {
            log.warn("Skipping order confirmation for orderId={}: could not resolve userId={}", event.orderId(), event.userId());
            return;
        }

        String subject = "Your order has been confirmed!";
        String body = String.format(
                "Hi %s,%n%nYour order %s has been confirmed and is being processed. It contains %d item(s).%n%nThank you for shopping with us!",
                user.firstName(), event.orderId(), event.items().size());

        sendAndRecord(user.id(), NotificationType.ORDER_CONFIRMATION, user.email(), subject, body, event.orderId());
    }

    @Override
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        AuthUserInfo user = fetchUser(event.userId());
        if (user == null) {
            log.warn("Skipping payment notification for orderId={}: could not resolve userId={}", event.orderId(), event.userId());
            return;
        }

        if (event.isSuccess()) {
            String subject = "Payment successful";
            String body = String.format(
                    "Hi %s,%n%nWe've successfully charged %s for order %s (transaction %s).",
                    user.firstName(), event.amount(), event.orderId(), event.transactionId());
            sendAndRecord(user.id(), NotificationType.PAYMENT_SUCCESS, user.email(), subject, body, event.orderId());
        } else {
            String subject = "Payment failed";
            String body = String.format(
                    "Hi %s,%n%nWe couldn't process your payment of %s for order %s. Please try again or use a different payment method.",
                    user.firstName(), event.amount(), event.orderId());
            sendAndRecord(user.id(), NotificationType.PAYMENT_FAILURE, user.email(), subject, body, event.orderId());
        }
    }

    @Override
    public void handleInventoryUpdated(InventoryUpdatedEvent event) {
        if (!event.lowStock()) {
            return; // only low-stock crossings are notification-worthy
        }

        String subject = "Low stock alert";
        String body = String.format(
                "Product %s is running low on stock: %d unit(s) remaining after the latest %s.",
                event.productId(), event.quantityRemaining(), event.movementType());

        // Admin-facing, not tied to a specific shopper — userId is null.
        sendAndRecord(null, NotificationType.LOW_STOCK_ALERT, adminAlertEmail, subject, body, event.productId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsForUser(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable).map(notificationMapper::toNotificationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(notificationMapper::toNotificationResponse);
    }

    private void sendAndRecord(UUID userId, NotificationType type, String email, String subject, String body, UUID relatedEntityId) {
        boolean sent = emailSender.send(email, subject, body);

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .recipientEmail(email)
                .subject(subject)
                .body(body)
                .status(sent ? NotificationStatus.SENT : NotificationStatus.FAILED)
                .relatedEntityId(relatedEntityId)
                .build();

        notificationRepository.save(notification);
    }

    private AuthUserInfo fetchUser(UUID userId) {
        try {
            return authServiceClient.getUserById(userId);
        } catch (FeignException e) {
            log.error("Failed to resolve user {} from auth-service: status={}", userId, e.status(), e);
            return null;
        }
    }
}
