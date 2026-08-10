package com.logistics.ai.global.exception;

import lombok.Getter;

@Getter
public class DeadlineGenerationRetryException extends RuntimeException {

	private final int retryCount;
	
	public DeadlineGenerationRetryException(String message, int retryCount, Throwable cause) {
		super(message, cause);
		this.retryCount = retryCount;
	}

	
}
