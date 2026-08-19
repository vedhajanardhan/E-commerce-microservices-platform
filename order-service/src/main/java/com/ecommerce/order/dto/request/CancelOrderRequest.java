package com.ecommerce.order.dto.request;

import jakarta.validation.constraints.Size;

public record CancelOrderRequest(

        @Size(max = 255)
        String reason
) {
}
