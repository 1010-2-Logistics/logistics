package com.logistics.ai.global.exception;

import lombok.Getter;

@Getter
public class AiException extends RuntimeException {

    private final ErrorCode errorCode;

    public AiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
