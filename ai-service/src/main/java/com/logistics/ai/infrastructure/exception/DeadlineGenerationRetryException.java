package com.logistics.ai.infrastructure.exception;

import lombok.Getter;

@Getter
public class DeadlineGenerationRetryException extends RuntimeException {

	private final int retryCount;
	
	private final String requestPrompt;
	
	private final String aiModel;
	
	public DeadlineGenerationRetryException(
			String message,
			int retryCount,
			String requestPrompt,
			String aiModel,
			Throwable cause
	) {
		super(message, cause);
		this.retryCount = retryCount;
		this.requestPrompt = requestPrompt;
		this.aiModel = aiModel;
	}

}
