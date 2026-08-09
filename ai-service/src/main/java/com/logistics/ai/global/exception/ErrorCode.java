package com.logistics.ai.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    // 구현체는 enum이라 name()은 Enum에서 자동으로 제공됩니다 (별도 구현 불필요).
    String name();

    HttpStatus getHttpStatus();

    String getMessage();
}
