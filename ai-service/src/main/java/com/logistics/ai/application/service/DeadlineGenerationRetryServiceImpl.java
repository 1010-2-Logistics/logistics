package com.logistics.ai.application.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.logistics.ai.application.dto.result.DispatchDeadlineResultDto;
import com.logistics.ai.application.dto.result.DispatchDeadlineRetryResultDto;
import com.logistics.ai.application.port.in.DeadlineGenerationRetryService;
import com.logistics.ai.application.port.out.DispatchDeadlineGenerationPort;
import com.logistics.ai.infrastructure.exception.DeadlineGenerationRetryException;
import com.logistics.ai.infrastructure.exception.NonRetryRemoteException;
import com.logistics.ai.infrastructure.exception.RemoteErrorCode;

@Service
public class DeadlineGenerationRetryServiceImpl implements DeadlineGenerationRetryService {

	private final DispatchDeadlineGenerationPort generationPort;
	
	private final RetryTemplate retryTemplate;
	
	public DeadlineGenerationRetryServiceImpl(
			DispatchDeadlineGenerationPort generationPort,
			@Qualifier("deadlineGenerationRetryTemplate") RetryTemplate retryTemplate
	) {
		this.generationPort = generationPort;
		this.retryTemplate = retryTemplate;
	}

	@Override
	public DispatchDeadlineRetryResultDto generate(String requestPrompt, String aiModel) {
		long startedAt = System.currentTimeMillis();
		
		AtomicInteger retryCount = new AtomicInteger(0);
		AtomicReference<String> lastRetryReason = new AtomicReference<>();
		
		try {
			return retryTemplate.execute(() -> {
				int currentRetryCount = retryCount.getAndIncrement();
				
				try {
					DispatchDeadlineResultDto result = generationPort.generate(requestPrompt, aiModel);
					
					int timeMs = Math.toIntExact(System.currentTimeMillis() - startedAt);
					
					return DispatchDeadlineRetryResultDto.of(
							result.responsePrompt(),
							result.finalDeadline(),
							timeMs,
							currentRetryCount,
							lastRetryReason.get()
					);
				} catch (Throwable t) {
					lastRetryReason.set(getRootMessage(t));
					
					if(isClientError(t)) {
						throw new NonRetryRemoteException(RemoteErrorCode.AI_API_4XX);
					}
					
					throw t;
				}
			});
		} catch (RetryException e) {
			Throwable lastException = e.getCause();
			
			throw new DeadlineGenerationRetryException(
					getRootMessage(lastException),
					e.getRetryCount(),
					requestPrompt,
					aiModel,
					lastException
			);
		}
		
	}
	
	private String getRootMessage(Throwable t) {
		if(t == null) {
			return "AI 호출 실패 원인을 찾을 수 없습니다.";
		}
		
		Throwable rootCause = t;
		
		while(rootCause.getCause() != null && rootCause.getCause() != rootCause) {
			rootCause = rootCause.getCause();
		}
		String message = rootCause.getMessage();
		
		return message == null || message.isBlank()
				? rootCause.getClass().getSimpleName()
						: message;
	}
	
	private boolean isClientError(Throwable t) {
		Throwable root = t;
		while (root != null) {
			if (root instanceof HttpClientErrorException) {
				return ((HttpClientErrorException) root).getStatusCode().is4xxClientError();
			}
			root = root.getCause();
		}
		return false;
	}
	
}
