package com.logistics.ai.infrastructure.feign.exception;

import lombok.Getter;

@Getter
public class RetryRemoteException extends RuntimeException {

	private final String code;
	
	public RetryRemoteException(String code, String message) {
		super(message);
		this.code = code;
	}
	
}
