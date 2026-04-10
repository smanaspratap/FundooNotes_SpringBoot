package com.fundoonotes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT utility for token generation and validation.
 *
 * Uses HMAC-SHA256 signing via the JJWT library.
 * Reads secret and expiration from externalized configuration.
 *
 * Designed for easy integration with Spring Security JWT filter in Part 2.
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generate a JWT token for the given user.
     *
     * @param userId the user's database ID
     * @param email  the user's email
     * @return signed JWT token string
     */
    public String generateToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();

        log.debug("JWT token generated for userId={}", userId);
        return token;
    }

    /**
     * Extract the user ID from a valid JWT token.
     *
     * @param token the JWT token string
     * @return the user ID stored in the token subject
     * @throws RuntimeException if the token is invalid or expired
     */
    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Validate whether a JWT token is well-formed and not expired.
     *
     * @param token the JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token");
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed");
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is null or empty");
        }
        return false;
    }

    /**
     * Get the remaining TTL (in milliseconds) of a valid token.
     * Used for blacklisting tokens with correct expiry duration.
     *
     * @param token the JWT token string
     * @return remaining TTL in milliseconds, or 0 if already expired
     */
    public long getRemainingTtlMs(String token) {
        try {
            Claims claims = parseClaims(token);
            long expiry = claims.getExpiration().getTime();
            long remaining = expiry - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse and return the claims from a JWT token.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
