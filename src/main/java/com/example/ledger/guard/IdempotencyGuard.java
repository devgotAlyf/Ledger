package com.example.ledger.guard;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class IdempotencyGuard {

    private static final Duration TTL = Duration.ofHours(24);
    private final StringRedisTemplate redisTemplate;

    public IdempotencyGuard(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryAcquire(String key) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "processing", TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public void markProcessed(String key) {
        redisTemplate.opsForValue().set(key, "processed", TTL);
    }

    public void release(String key) {
        redisTemplate.delete(key);
    }
}
