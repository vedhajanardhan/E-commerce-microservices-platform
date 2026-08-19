-- V1__init_schema.sql
-- Inventory Service schema: inventory_items, stock_movements, processed_order_events.

CREATE TABLE inventory_items (
    product_id         UUID PRIMARY KEY,
    sku                VARCHAR(50) NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0 CHECK (quantity_available >= 0),
    reorder_threshold  INT NOT NULL DEFAULT 10,
    version            BIGINT NOT NULL DEFAULT 0,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stock_movements (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL,
    movement_type       VARCHAR(20) NOT NULL,
    quantity            INT NOT NULL,
    resulting_quantity  INT NOT NULL,
    reason              VARCHAR(255),
    order_id            UUID,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_movements_product_id ON stock_movements (product_id);
CREATE INDEX idx_stock_movements_order_id ON stock_movements (order_id);

-- Idempotency guard: one row per successfully-processed order-created
-- event, so a redelivered Kafka message is a no-op instead of a double
-- decrement.
CREATE TABLE processed_order_events (
    order_id     UUID PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
