-- V2__add_order_cancellation_idempotency.sql

CREATE TABLE processed_order_cancellations (
    order_id     UUID PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
