package com.logistics.inventory.application.port;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyPort {
    // 멱등 키 잡기
    boolean acquire(
            String key,
            Duration ttl
    );

    // 키 삭제
    void release(String key);

    // 전에 성공했나?
    <T> Optional<T> getResult(
            String key,
            Class<T> resultType
            );

    // 성공 결과로 멱등키 완성
    void complete(
            String key,
            Object result,
            Duration ttl
    );
}
