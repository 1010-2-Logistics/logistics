package com.logistics.ai.infrastructure.feign.request;

import java.util.List;

public record AiRequestDto(
		List<Content> contents
) {

	public record Content(
      List<Part> parts
  ) {}
	
	public record Part(
      String text
  ) {}
	
	public static AiRequestDto from(String fullPrompt) {
    return new AiRequestDto(
        List.of(new Content(List.of(new Part(fullPrompt))))
    );
	}
}
