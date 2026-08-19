package com.ecommerce.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtValidatorTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B59";

    private JwtValidator jwtValidator;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtValidator = new JwtValidator(SECRET);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void validToken_extractsUserIdAndRoles() {
        String token = Jwts.builder()
                .subject("user-123")
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact();

        Claims claims = jwtValidator.validateAndExtractClaims(token);

        assertEquals("user-123", jwtValidator.extractUserId(claims));
        assertEquals(List.of("ROLE_USER"), jwtValidator.extractRoles(claims));
    }

    @Test
    void expiredToken_throwsJwtException() {
        String expiredToken = Jwts.builder()
                .subject("user-123")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(signingKey)
                .compact();

        assertThrows(JwtException.class, () -> jwtValidator.validateAndExtractClaims(expiredToken));
    }

    @Test
    void tamperedToken_throwsJwtException() {
        String tampered = Jwts.builder()
                .subject("user-123")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey)
                .compact() + "tampered";

        assertThrows(JwtException.class, () -> jwtValidator.validateAndExtractClaims(tampered));
    }
}
