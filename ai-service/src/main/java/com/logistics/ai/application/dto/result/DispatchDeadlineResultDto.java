package com.logistics.ai.application.dto.result;

import java.time.LocalDateTime;

public record DispatchDeadlineResultDto(
		String responsePrompt,
		LocalDateTime finalDeadline
) {

	public static DispatchDeadlineResultDto of(String responsePrompt, LocalDateTime finalDeadline) {
		return new DispatchDeadlineResultDto(
				responsePrompt,
				finalDeadline
		);
	}
}
