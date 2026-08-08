package com.logistics.inventory.application.port;

import java.time.Duration;

public interface IdempotencyPort {
    boolean acquire(
            String key,
            Duration ttl
    );

    void release(String key);
}
