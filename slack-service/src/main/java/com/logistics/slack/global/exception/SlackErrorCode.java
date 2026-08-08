package com.logistics.slack.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SlackErrorCode implements ErrorCode {
    SLACK_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Slack 메시지 발송 이력을 찾을 수 없습니다"
    ),
    SLACK_RETRY_STATUS_CONFLICT(
            HttpStatus.CONFLICT,
            "FAILED 상태의 Slack 메시지만 재발송할 수 있습니다."
    ),
    SLACK_RETRY_LIMIT_CONFLICT(
            HttpStatus.CONFLICT,
            "최대 재시도 횟수를 초과했습니다."
    ),
    SLACK_DELETED_CONFLICT(
            HttpStatus.CONFLICT,
            "삭제된 Slack 메시지는 재발송할 수 없습니다."
    ),
    SLACK_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
