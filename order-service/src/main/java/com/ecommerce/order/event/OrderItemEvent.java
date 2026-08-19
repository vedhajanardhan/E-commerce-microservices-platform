package com.ecommerce.order.event;

import java.util.UUID;

public record OrderItemEvent(
        UUID productId,
        int quantity
) {
}
