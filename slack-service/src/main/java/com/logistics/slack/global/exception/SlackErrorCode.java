package com.logistics.slack.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SlackErrorCode implements ErrorCode {

    SAMPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 샘플입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
