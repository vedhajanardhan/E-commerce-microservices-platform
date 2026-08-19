package com.ecommerce.notification.dto.response;

import com.ecommerce.notification.entity.NotificationStatus;
import com.ecommerce.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        NotificationType type,
        String recipientEmail,
        String subject,
        NotificationStatus status,
        UUID relatedEntityId,
        LocalDateTime createdAt
) {
}
