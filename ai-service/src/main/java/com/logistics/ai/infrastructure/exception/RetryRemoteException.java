package com.logistics.ai.infrastructure.exception;

import lombok.Getter;

@Getter
public class RetryRemoteException extends RuntimeException {

	private final String code;
	
	private final String message;
	
	private final RemoteErrorCode error;
	
	public RetryRemoteException(RemoteErrorCode error, String message) {
		super(message);
		this.code = error.name();
		this.error = error;
		this.message = message;
	}
	
}
