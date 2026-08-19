package com.ecommerce.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-for-jwt-signing-in-unit-and-integration-tests-only";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 900000L);
    }

    @Test
    void generateAccessToken_thenParseClaims_roundTripsCorrectly() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessToken(userId, "vedha", List.of("ROLE_USER"));

        Claims claims = jwtUtil.parseClaims(token);

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("vedha", claims.get("username"));
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_withGarbageToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("not-a-real-jwt"));
    }

    @Test
    void parseClaims_withTokenSignedByDifferentSecret_throwsJwtException() {
        JwtUtil otherUtil = new JwtUtil("a-completely-different-secret-key-value-here", 900000L);
        String token = otherUtil.generateAccessToken(UUID.randomUUID(), "vedha", List.of("ROLE_USER"));

        assertThrows(JwtException.class, () -> jwtUtil.parseClaims(token));
    }
}
