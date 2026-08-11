package com.logistics.order.application.port;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyPort {
    // 멱등 키 잡기
    boolean acquire(
            String key,
            Duration ttl
    );

    // 실패 시 선점한 키 삭제
    void release(String key);

    // 이전에 성공한 요청인지 조회
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
