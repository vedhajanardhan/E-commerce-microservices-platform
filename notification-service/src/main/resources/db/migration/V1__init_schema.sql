-- V1__init_schema.sql
-- Notification Service schema: notifications.

CREATE TABLE notifications (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID,
    type              VARCHAR(30) NOT NULL,
    recipient_email   VARCHAR(150) NOT NULL,
    subject           VARCHAR(200) NOT NULL,
    body              VARCHAR(2000) NOT NULL,
    status            VARCHAR(10) NOT NULL,
    related_entity_id UUID,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_related_entity_id ON notifications (related_entity_id);
