package com.ecommerce.auth.entity;

/**
 * Fixed role vocabulary for the platform. Kept as an enum (backed by a
 * lookup table via {@link Role}) rather than a free-text column so role
 * checks throughout the codebase and downstream services are type-safe.
 */
public enum ERole {
    ROLE_USER,
    ROLE_ADMIN
}
