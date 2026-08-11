package com.logistics.ai.infrastructure.exception;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RemoteErrorCode {
	// Delivery
	DELIVERY_HUB_ROUTE_NOT_FOUND(
			"경유 허브 정보를 찾을 수 없습니다."
	),
	
	DELIVERY_REMOTE_ERROR(
			"Delivery Service 통신 장애"
	),
	
	// Hub
	HUB_INFO_NOT_FOUND(
			"허브 정보를 찾을 수 없습니다."
	),
	
	HUB_REMOTE_ERROR(
			"Hub Service 통신 장애"
	),
	
	// Product
	PRODUCT_INFO_NOT_FOUND(
			"상품 정보를 찾을 수 없습니다."
	),
	
	PRODUCT_REMOTE_ERROR(
			"Product Service 통신 장애"
	)
	
	;
	private final String message;

	public static RemoteErrorCode from(String code) {
		return Arrays.stream(values())
				.filter(v -> v.name().equals(code))
				.findFirst()
				.orElse(null);
	}

}
