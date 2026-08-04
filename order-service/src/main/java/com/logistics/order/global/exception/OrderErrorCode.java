package com.logistics.order.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// 실제 서비스로 복사할 때 SampleErrorCode -> {Domain}ErrorCode 로 이름 바꾸고,
// 네이밍 컨벤션({도메인명}_{에러타입})에 맞춰 항목을 채우세요. (예: HUB_NOT_FOUND)
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_INVALID_REQUEST(HttpStatus.NOT_FOUND, "요청값이 올바르지 않습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;
}
