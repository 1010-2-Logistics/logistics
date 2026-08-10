package com.logistics.ai.application.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;

import com.logistics.ai.application.dto.result.DispatchDeadlineResultDto;
import com.logistics.ai.application.dto.result.DispatchDeadlineRetryResultDto;
import com.logistics.ai.application.port.in.DeadlineGenerationRetryService;
import com.logistics.ai.application.port.out.DispatchDeadlineGenerationPort;
import com.logistics.ai.global.exception.DeadlineGenerationRetryException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeadlineGenerationRetryServiceImpl implements DeadlineGenerationRetryService {

	private final DispatchDeadlineGenerationPort generationPort;
	
	private final RetryTemplate retryTemplate;

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
					
					throw t;
				}
			});
		} catch (RetryException e) {
			Throwable lastException = e.getCause();
			
			throw new DeadlineGenerationRetryException(
					getRootMessage(lastException),
					e.getRetryCount(),
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
	
	
}
