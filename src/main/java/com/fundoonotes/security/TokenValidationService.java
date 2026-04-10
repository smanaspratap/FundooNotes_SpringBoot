package com.fundoonotes.security;

import com.fundoonotes.cache.TokenCacheService;
import com.fundoonotes.exception.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Centralized token validation service.
 * Wraps JwtUtil + Redis blacklist check into a single reusable component.
 *
 * All services should use this instead of calling JwtUtil directly.
 * This keeps auth logic in the security boundary, not in controllers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final JwtUtil jwtUtil;
    private final TokenCacheService tokenCacheService;

    /**
     * Validate a token and extract the userId.
     *
     * @param token the JWT token from the Authorization header
     * @return the userId from the token
     * @throws UnauthorizedAccessException if token is missing, blacklisted, or invalid
     */
    public Long validateAndExtractUserId(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Missing or empty authorization token");
            throw new UnauthorizedAccessException("Authorization token is required");
        }

        // Check blacklist first (logged-out tokens)
        if (tokenCacheService.isTokenBlacklisted(token)) {
            log.warn("Attempt to use a blacklisted (logged-out) token");
            throw new UnauthorizedAccessException("Token has been invalidated");
        }

        // Validate JWT signature and expiry
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired authorization token");
            throw new UnauthorizedAccessException("Invalid or expired token");
        }

        return jwtUtil.extractUserId(token);
    }

    /**
     * Extract remaining TTL (in milliseconds) from a valid token.
     * Used for blacklisting with correct expiry.
     *
     * @param token the JWT token
     * @return remaining TTL in milliseconds, or 0 if already expired
     */
    public long getRemainingTtlMs(String token) {
        return jwtUtil.getRemainingTtlMs(token);
    }
}
