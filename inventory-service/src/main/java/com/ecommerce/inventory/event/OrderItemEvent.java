package com.ecommerce.inventory.event;

import java.util.UUID;

public record OrderItemEvent(
        UUID productId,
        int quantity
) {
}
