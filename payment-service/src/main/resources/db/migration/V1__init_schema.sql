-- V1__init_schema.sql
-- Payment Service schema: payments.

CREATE TABLE payments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id         UUID NOT NULL,
    user_id          UUID NOT NULL,
    amount           NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    status           VARCHAR(20) NOT NULL,
    transaction_id   VARCHAR(100) UNIQUE,
    failure_reason   VARCHAR(255),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_user_id ON payments (user_id);
