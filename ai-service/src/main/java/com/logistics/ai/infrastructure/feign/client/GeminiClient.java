package com.logistics.ai.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.logistics.ai.infrastructure.config.AiFeignConfig;
import com.logistics.ai.infrastructure.feign.request.AiRequestDto;
import com.logistics.ai.infrastructure.feign.response.AiResponseDto;

@FeignClient(
		name = "gemini-service",
		url = "${ai.gemini.url}",
		configuration = AiFeignConfig.class
)
public interface GeminiClient {

	@PostMapping("/{model}:generateContent")
	AiResponseDto generateText(
			@PathVariable("model") String model,
			@RequestBody AiRequestDto request
	);
	
	
}
