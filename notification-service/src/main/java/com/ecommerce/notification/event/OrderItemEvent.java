package com.ecommerce.notification.event;

import java.util.UUID;

public record OrderItemEvent(
        UUID productId,
        int quantity
) {
}
