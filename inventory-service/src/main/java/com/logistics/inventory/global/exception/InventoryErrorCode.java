package com.logistics.inventory.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품과 허브의 재고를 찾을 수 없습니다."),
    INVENTORY_OUT_OF_STOCK(HttpStatus.CONFLICT, "차감할 재고 수량이 부족합니다."),
    INVENTORY_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "차감 수량 또는 요청값이 올바르지 않습니다.")


    ;

    private final HttpStatus httpStatus;
    private final String message;
}
