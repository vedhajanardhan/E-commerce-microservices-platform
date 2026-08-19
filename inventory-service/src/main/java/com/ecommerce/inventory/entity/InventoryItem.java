package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One row per product. The id here IS product-service's product id
 * (same cross-service 1:1-by-shared-key pattern used for UserProfile in
 * user-service) — no foreign key across the database boundary.
 * <p>
 * {@code @Version} enables optimistic locking so concurrent
 * increase/decrease operations on the same product (e.g. two orders
 * checking out at once) fail fast with an OptimisticLockException
 * instead of silently overwriting each other's quantity change.
 */
@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    private UUID productId;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(name = "quantity_available", nullable = false)
    private int quantityAvailable;

    @Builder.Default
    @Column(name = "reorder_threshold", nullable = false)
    private int reorderThreshold = 10;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isLowStock() {
        return quantityAvailable <= reorderThreshold;
    }
}
