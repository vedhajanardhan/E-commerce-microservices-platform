package com.ecommerce.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Separate from {@link ProcessedOrderEvent} (which guards the creation/
 * decrement path) because a cancellation is a distinct event for the
 * same orderId — reusing one table would make it impossible to tell
 * "already decremented" apart from "already restocked".
 */
@Entity
@Table(name = "processed_order_cancellations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrderCancellation {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private LocalDateTime processedAt;

    public ProcessedOrderCancellation(UUID orderId) {
        this.orderId = orderId;
    }
}
