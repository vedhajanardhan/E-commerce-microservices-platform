package com.ecommerce.notification.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        List<OrderItemEvent> items,
        Instant createdAt
) {
}
