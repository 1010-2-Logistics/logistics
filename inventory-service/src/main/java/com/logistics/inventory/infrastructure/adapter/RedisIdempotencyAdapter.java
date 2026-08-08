package com.logistics.inventory.infrastructure.adapter;


import com.logistics.inventory.application.port.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IdempotencyPort {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean acquire(
            String key,
            Duration ttl
    ) {
        Boolean result = stringRedisTemplate.opsForValue()
                .setIfAbsent( // <- SETNX 역할
                        key,
                        "1",
                        ttl
                );

        return Boolean.TRUE.equals(result);
    }

    @Override
    public void release(String key) {
        stringRedisTemplate.delete(key);
    }
}
