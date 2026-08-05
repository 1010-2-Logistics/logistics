package com.logistics.user.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * User 도메인에서 발생하는 오류 코드를 관리한다.
 *
 * 공통 CustomException이 ErrorCode 타입을 받기 때문에
 * 반드시 ErrorCode 인터페이스를 구현해야 한다.
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "사용자를 찾을 수 없습니다."
    ),
    USER_USERNAME_DUPLICATED(
            HttpStatus.CONFLICT,
            "이미 사용 중인 username입니다."
    ),

    USER_SLACK_ID_DUPLICATED(
            HttpStatus.CONFLICT,
            "이미 사용 중인 Slack ID입니다."
    ),

    USER_PASSWORD_CONFIRM_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "비밀번호와 비밀번호 확인값이 일치하지 않습니다."
    ),
    USER_APPROVAL_INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "승인 요청 형식이 올바르지 않습니다."
    ),

    USER_APPROVAL_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "사용자 가입 승인 또는 거절 권한이 없습니다."
    ),

    USER_APPROVAL_CONFLICT(
            HttpStatus.CONFLICT,
            "이미 처리된 가입 신청입니다."
    ),
    AUTH_UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "인증 토큰이 없거나 유효하지 않습니다."
    ),
    USER_DELETED_CONFLICT(
            HttpStatus.CONFLICT,
            "삭제된 사용자는 가입 승인 또는 거절할 수 없습니다."
    );


    private final HttpStatus httpStatus;
    private final String message;
}