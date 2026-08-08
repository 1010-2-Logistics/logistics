package com.logistics.inventory.infrastructure.adapter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.inventory.application.port.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IdempotencyPort {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean acquire(
            String key,
            Duration ttl
    ) {
        Boolean result = stringRedisTemplate.opsForValue()
                // 키가 없을 때만, inventory:deduct:orderId = PROCESSING 저장
                .setIfAbsent(key, "PROCESSING", ttl);

        return Boolean.TRUE.equals(result);
    }

    @Override
    public void release(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public <T> Optional<T> getResult(
            String key,
            Class<T> resultType
    ) {
        String value = stringRedisTemplate.opsForValue().get(key);

        if (value == null || value.equals("PROCESSING")) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(
                    value,
                    resultType
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void complete(
            String key,
            Object result,
            Duration ttl
    ) {
        try {
            String value = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(key, value, ttl);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
