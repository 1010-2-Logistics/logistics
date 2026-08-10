package com.logistics.ai.infrastructure.exception;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RemoteErrorCode {
	// Delivery
	DELIVERY_NOT_FOUND(false),
	
	// Hub
	HUB_NOT_FOUND(false),
	
	
	;
	private final boolean retry;

	public static RemoteErrorCode from(String code) {
		return Arrays.stream(values())
				.filter(v -> v.name().equals(code))
				.findFirst()
				.orElse(null);
	}

}
