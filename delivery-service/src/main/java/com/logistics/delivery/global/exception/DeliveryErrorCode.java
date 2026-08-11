package com.logistics.delivery.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {

    DELIVERY_MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 배송담당자입니다."),
    DELIVERY_INVALID_HUB_ID(HttpStatus.BAD_REQUEST, "존재하지 않는 허브 ID입니다."),
    DELIVERY_FORBIDDEN(HttpStatus.FORBIDDEN, "등록 권한이 없습니다."),
    DELIVERY_MANAGER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 배송 담당자입니다."),
    DELIVERY_MANAGER_TYPE_HUB_MISMATCH(HttpStatus.BAD_REQUEST, "담당자 타입과 허브 ID 조합이 올바르지 않습니다."),
    DELIVERY_HUB_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Hub 서비스에 연결할 수 없습니다."),
    DELIVERY_HUB_ROUTE_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "HubRoute 서비스에 연결할 수 없습니다."),
    DELIVERY_USER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "User 서비스에 연결할 수 없습니다."),
    DELIVERY_MANAGER_UNAVAILABLE(HttpStatus.CONFLICT, "배정 가능한 담당자가 없습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 배송입니다."),
    DELIVERY_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 전이입니다."),
    DELIVERY_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 배송 경로입니다."),
    DELIVERY_LOCK_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "배정 처리가 지연되고 있습니다. 잠시 후 다시 시도해주세요."),
    DELIVERY_HUB_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "허브 간 연결된 경로를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}