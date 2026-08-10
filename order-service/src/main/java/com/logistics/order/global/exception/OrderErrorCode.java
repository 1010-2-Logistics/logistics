package com.logistics.order.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다."),
    ORDER_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 주문입니다."),
    ORDER_DELETE_CONFLICT(HttpStatus.CONFLICT, "이미 삭제된 주문입니다."),
    ORDER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "연동 서비스 처리에 실패했습니다."),
    ORDER_CANCEL_CONFLICT(HttpStatus.CONFLICT, "이미 취소됐거나 배송이 시작되어 취소할 수 없습니다."),
    ORDER_ALREADY_PROCESSING(HttpStatus.CONFLICT, "동일한 주문 생성 요청이 처리 중입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;
}
