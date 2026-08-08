package com.logistics.ai.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.port.out.DispatchDeadlineGenerationPort;
import com.logistics.ai.infrastructure.feign.client.GeminiClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DispatchDeadlineGenerationAdapter implements DispatchDeadlineGenerationPort {

	private final GeminiClient geminiClient;
}
