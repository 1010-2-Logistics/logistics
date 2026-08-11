package com.logistics.ai.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements ErrorCode {
	
	AI_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 정보를 불러오는데 실패했습니다."),
	
	AI_HUB_INFO_INCOMPLETE(HttpStatus.INTERNAL_SERVER_ERROR, "허브 정보를 불러오는데 실패했습니다."),
	
	AI_DELIVERY_ROUTE_EMPTY(HttpStatus.NOT_FOUND, "경유 허브 목록을 불러오는데 실패했습니다."),

	AI_DLQ_REDRIVE_COUNT_INVALID(HttpStatus.BAD_REQUEST, "재처리 요청 개수는 1 이상 10 이하여야 합니다."),
	;
	
  private final HttpStatus httpStatus;
  private final String message;
}
