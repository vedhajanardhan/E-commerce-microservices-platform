package com.ecommerce.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product name/price are denormalized (copied) onto the cart item at
 * add-time rather than looked up fresh on every cart view. This means
 * the cart shows the price the shopper saw when they added the item,
 * even if product-service's price changes later — the same "price
 * honored at add-to-cart" behavior most real storefronts have. The
 * price actually charged is still re-validated against product-service
 * at checkout in order-service, so this denormalization is purely a
 * display/UX convenience, not a source of truth for billing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {

    private UUID productId;
    private String sku;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
