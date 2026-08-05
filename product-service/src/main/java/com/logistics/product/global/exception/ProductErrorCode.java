package com.logistics.product.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 실제 서비스로 복사할 때 SampleErrorCode -> {Domain}ErrorCode 로 이름 바꾸고,
// 네이밍 컨벤션({도메인명}_{에러타입})에 맞춰 항목을 채우세요. (예: HUB_NOT_FOUND)
@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
  
  PRODUCT_DELETED_CONFLICT(HttpStatus.CONFLICT, "삭제된 상품입니다."),
  
  PRODUCT_COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "업체 정보가 존재하지 않습니다."),
  
  PRODUCT_INVALID_PRODUCER_COMPANY_TYPE(HttpStatus.CONFLICT, "생산 업체만 상품 등록이 가능합니다."),
  
  
  PRODUCT_EXISTS_PRODUCT_NAME(HttpStatus.CONFLICT, "해당 업체에 이미 존재하는 상품입니다."),
	;
	
  private final HttpStatus httpStatus;
  private final String message;
}
