package com.logistics.slack.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SlackErrorCode implements ErrorCode {

    SLACK_NOT_FOUND(HttpStatus.NOT_FOUND, "Slack 메시지 발송 이력을 찾을 수 없습니다");

    private final HttpStatus httpStatus;
    private final String message;
}
