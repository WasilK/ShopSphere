package com.wasil.ShopSphere.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void shouldBlacklistTokenWithCorrectKeyAndTtl() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        service.blacklist("abc-123", 60_000L);

        verify(valueOperations).set(
                eq("jwt:blacklist:abc-123"),
                eq("revoked"),
                eq(Duration.ofMillis(60_000L))
        );
    }

    @Test
    void shouldNotBlacklistWhenJtiIsNull() {

        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        service.blacklist(null, 60_000L);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldNotBlacklistWhenJtiIsBlank() {

        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        service.blacklist("   ", 60_000L);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldNotBlacklistWhenTtlIsZeroOrNegative() {

        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        service.blacklist("abc-123", 0);
        service.blacklist("abc-123", -500);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void shouldReportTokenAsBlacklistedWhenKeyExists() {

        when(redisTemplate.hasKey("jwt:blacklist:abc-123"))
                .thenReturn(true);

        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        assertTrue(service.isBlacklisted("abc-123"));
    }

    @Test
    void shouldReportTokenAsNotBlacklistedWhenKeyMissing() {

        when(redisTemplate.hasKey("jwt:blacklist:abc-123"))
                .thenReturn(false);

        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        assertFalse(service.isBlacklisted("abc-123"));
    }

    @Test
    void shouldReturnFalseForBlankJtiWithoutQueryingRedis() {

        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        assertFalse(service.isBlacklisted(null));
        assertFalse(service.isBlacklisted(""));

        verify(redisTemplate, never()).hasKey(anyString());
    }
}
