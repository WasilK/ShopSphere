package com.wasil.ShopSphere.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

/*
 * Backs real (non-cosmetic) logout for stateless JWTs.
 *
 * A JWT can't be "deleted" — the server never stored it — so instead we
 * record that a given token's jti has been revoked, and reject it on
 * every subsequent request until it would have expired anyway. Storing
 * this in Redis (rather than an in-memory Set/Map) means it survives
 * app restarts and works correctly if this service is ever scaled to
 * more than one instance, since every instance shares the same Redis.
 */
@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /*
     * Revoke a single token by its jti. ttlMillis should be the token's
     * remaining natural validity (JwtService.getRemainingValidityMillis) —
     * Redis expires the entry automatically once the token would have
     * stopped being valid anyway, so we never accumulate stale entries.
     */
    public void blacklist(String jti, long ttlMillis) {

        if (!StringUtils.hasText(jti) || ttlMillis <= 0) {
            // Nothing to do: either this token predates the jti claim
            // (can't be individually revoked) or it's already expired.
            return;
        }

        redisTemplate.opsForValue().set(
                KEY_PREFIX + jti,
                "revoked",
                Duration.ofMillis(ttlMillis)
        );
    }

    public boolean isBlacklisted(String jti) {

        if (!StringUtils.hasText(jti)) {
            return false;
        }

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(KEY_PREFIX + jti)
        );
    }
}
