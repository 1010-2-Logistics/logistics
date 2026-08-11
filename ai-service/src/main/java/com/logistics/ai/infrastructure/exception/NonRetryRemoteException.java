package com.logistics.ai.infrastructure.exception;

import lombok.Getter;

@Getter
public class NonRetryRemoteException extends RuntimeException {

	private final String code;
	
	private final RemoteErrorCode error;
	
	public NonRetryRemoteException(RemoteErrorCode error) {
		super(error.getMessage());
		this.code = error.name();
		this.error = error;
	}
	
}
