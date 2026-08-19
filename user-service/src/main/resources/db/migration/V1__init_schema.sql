-- V1__init_schema.sql
-- User Service schema: user_profiles, addresses.
-- Note: user_profiles.id is NOT auto-generated here — it is always set
-- by the application to match the corresponding auth-service user id
-- (1:1 relationship maintained at the application layer, since a real
-- foreign key across service/database boundaries would violate the
-- database-per-service pattern).

CREATE TABLE user_profiles (
    id             UUID PRIMARY KEY,
    phone          VARCHAR(20),
    avatar_url     VARCHAR(512),
    date_of_birth  DATE,
    gender         VARCHAR(20),
    bio            VARCHAR(500),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE addresses (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_profile_id  UUID         NOT NULL REFERENCES user_profiles (id) ON DELETE CASCADE,
    address_line1    VARCHAR(200) NOT NULL,
    address_line2    VARCHAR(200),
    city             VARCHAR(100) NOT NULL,
    state            VARCHAR(100) NOT NULL,
    postal_code      VARCHAR(20)  NOT NULL,
    country          VARCHAR(100) NOT NULL,
    address_type     VARCHAR(20)  NOT NULL,
    is_default       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_addresses_user_profile_id ON addresses (user_profile_id);
