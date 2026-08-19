-- V1__init_schema.sql
-- Order Service schema: orders, order_items.

CREATE TABLE orders (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL,
    status                  VARCHAR(20) NOT NULL,
    total_amount            NUMERIC(12,2) NOT NULL CHECK (total_amount >= 0),
    shipping_address        VARCHAR(500),
    payment_transaction_id  VARCHAR(100),
    cancellation_reason     VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id   UUID NOT NULL,
    sku          VARCHAR(50),
    product_name VARCHAR(200) NOT NULL,
    unit_price   NUMERIC(12,2) NOT NULL CHECK (unit_price > 0),
    quantity     INT NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
