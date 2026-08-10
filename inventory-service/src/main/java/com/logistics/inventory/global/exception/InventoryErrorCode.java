package com.logistics.inventory.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품과 허브의 재고를 찾을 수 없습니다."),
    INVENTORY_OUT_OF_STOCK(HttpStatus.CONFLICT, "차감할 재고 수량이 부족합니다."),
    INVENTORY_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "차감 수량 또는 요청값이 올바르지 않습니다."),
    INVENTORY_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    INVENTORY_HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "허브를 찾을 수 없습니다."),
    INVENTORY_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 재고 요청입니다."),
    INVENTORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 허브에 동일한 상품 재고가 이미 존재합니다."),



    ;

    private final HttpStatus httpStatus;
    private final String message;
}
