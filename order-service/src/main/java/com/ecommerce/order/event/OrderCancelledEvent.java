package com.ecommerce.order.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        List<OrderItemEvent> items,
        String reason,
        Instant timestamp
) {
}
