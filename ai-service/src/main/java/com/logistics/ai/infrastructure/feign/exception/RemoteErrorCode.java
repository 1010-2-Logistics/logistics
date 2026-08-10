package com.logistics.ai.infrastructure.feign.exception;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RemoteErrorCode {
	;
	private final boolean retry;

	public static RemoteErrorCode from(String code) {
		return Arrays.stream(values())
				.filter(v -> v.name().equals(code))
				.findFirst()
				.orElse(null);
	}

}
