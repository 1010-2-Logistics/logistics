package com.logistics.ai.infrastructure.feign.exception;

import lombok.Getter;

@Getter
public class NonRetryRemoteException extends RuntimeException {

	private final String code;
	
	public NonRetryRemoteException(String code, String message) {
		super(message);
		this.code = code;
	}
	
}
