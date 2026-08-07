package com.logistics.user.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증 및 로그인 과정에서 발생하는 오류 코드.
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    /**
     * username 또는 password가 누락된 경우.
     */
    AUTH_LOGIN_INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "로그인 요청 형식이 올바르지 않습니다."
    ),

    /**
     * 아이디가 없거나 비밀번호가 틀린 경우.
     */
    AUTH_INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "사용자 아이디 또는 비밀번호가 올바르지 않습니다."
    ),

    /**
     * 회원가입 승인 전인 사용자.
     */
    AUTH_APPROVAL_PENDING(
            HttpStatus.FORBIDDEN,
            "가입 승인 대기 상태입니다."
    ),

    /**
     * 가입 신청이 거절된 사용자.
     */
    AUTH_APPROVAL_REJECTED(
            HttpStatus.FORBIDDEN,
            "가입이 거절된 사용자입니다."
    ),

    /**
     * Soft Delete 처리된 사용자.
     */
    AUTH_USER_DELETED(
            HttpStatus.FORBIDDEN,
            "삭제된 사용자는 로그인할 수 없습니다."
    ),

    /**
     * 추후 Gateway 또는 인증 API에서 사용하는 공통 401 오류.
     */
    AUTH_UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "인증 토큰이 없거나 유효하지 않습니다."
    ),
    AUTH_INVALID_GATEWAY_HEADER(
            HttpStatus.UNAUTHORIZED,
            "Gateway 인증 헤더 형식이 올바르지 않습니다."
    );

    private final HttpStatus httpStatus;
    private final String message;
}