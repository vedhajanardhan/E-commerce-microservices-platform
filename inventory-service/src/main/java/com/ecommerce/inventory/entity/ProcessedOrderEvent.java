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
 * Kafka delivers messages at-least-once — a broker restart, consumer
 * rebalance, or retry can redeliver the same order-created event. Before
 * decrementing stock, the listener checks this table for the orderId; if
 * present, the event is a duplicate and is skipped. This is the standard
 * "idempotent consumer" pattern for event-driven stock decrements.
 */
@Entity
@Table(name = "processed_order_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrderEvent {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private LocalDateTime processedAt;

    public ProcessedOrderEvent(UUID orderId) {
        this.orderId = orderId;
    }
}
