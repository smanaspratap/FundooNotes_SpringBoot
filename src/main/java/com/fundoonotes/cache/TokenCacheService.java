package com.fundoonotes.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for managing token/OTP data in Redis with TTL-based expiry.
 *
 * Key naming strategy:
 *   fundoo:verify:<email>     → verification token (TTL: 15 min)
 *   fundoo:reset:<email>      → OTP string          (TTL: 10 min)
 *   fundoo:blacklist:<jwt>    → "true"              (TTL: remaining JWT life)
 *
 * SECURITY: This service NEVER logs OTP values, reset tokens, or raw JWT tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String VERIFY_PREFIX = "fundoo:verify:";
    private static final String RESET_PREFIX = "fundoo:reset:";
    private static final String BLACKLIST_PREFIX = "fundoo:blacklist:";

    // =========================================================================
    // Verification token
    // =========================================================================

    public void storeVerificationToken(String email, String token, long ttlMinutes) {
        String key = VERIFY_PREFIX + email;
        redisTemplate.opsForValue().set(key, token, ttlMinutes, TimeUnit.MINUTES);
        log.info("Verification token stored for email={} (TTL={}min)", email, ttlMinutes);
    }

    public String getVerificationToken(String email) {
        String key = VERIFY_PREFIX + email;
        String token = redisTemplate.opsForValue().get(key);
        log.debug("Verification token lookup for email={}: {}", email, token != null ? "found" : "not found");
        return token;
    }

    // =========================================================================
    // Reset OTP
    // =========================================================================

    public void storeResetOtp(String email, String otp, long ttlMinutes) {
        String key = RESET_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, ttlMinutes, TimeUnit.MINUTES);
        log.info("Reset OTP stored for email={} (TTL={}min)", email, ttlMinutes);
    }

    public String getResetOtp(String email) {
        String key = RESET_PREFIX + email;
        String otp = redisTemplate.opsForValue().get(key);
        log.debug("Reset OTP lookup for email={}: {}", email, otp != null ? "found" : "not found");
        return otp;
    }

    public void deleteResetOtp(String email) {
        String key = RESET_PREFIX + email;
        redisTemplate.delete(key);
        log.info("Reset OTP deleted for email={}", email);
    }

    // =========================================================================
    // Token blacklist (logout invalidation)
    // =========================================================================

    public void blacklistToken(String jwtToken, long ttlMs) {
        String key = BLACKLIST_PREFIX + jwtToken;
        redisTemplate.opsForValue().set(key, "true", ttlMs, TimeUnit.MILLISECONDS);
        log.info("Token blacklisted (TTL={}ms)", ttlMs);
    }

    public boolean isTokenBlacklisted(String jwtToken) {
        String key = BLACKLIST_PREFIX + jwtToken;
        boolean blacklisted = Boolean.TRUE.toString().equals(redisTemplate.opsForValue().get(key));
        if (blacklisted) {
            log.debug("Token is blacklisted");
        }
        return blacklisted;
    }
}
