package com.logistics.ai.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.result.DispatchDeadlineResultDto;
import com.logistics.ai.application.port.out.DispatchDeadlineGenerationPort;
import com.logistics.ai.infrastructure.feign.client.GeminiClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DispatchDeadlineGenerationAdapter implements DispatchDeadlineGenerationPort {

	private final GeminiClient geminiClient;

	@Override
	public DispatchDeadlineResultDto generate(String requestPrompt, String aiModel) {
		return null;
	}
}
