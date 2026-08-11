package com.logistics.ai.application.dto.result;

import java.time.LocalDateTime;

public record DispatchDeadlineRetryResultDto(
		String responsePrompt,
		LocalDateTime finalDeadline,
		int timeMs,
		int retryCount,
		String lastRetryReason
) {

	public static DispatchDeadlineRetryResultDto of(
			String responsePrompt,
			LocalDateTime finalDeadline,
			int timeMs,
			int retryCount,
			String lastRetryReason
	) {
		
		return new DispatchDeadlineRetryResultDto(
				responsePrompt,
				finalDeadline,
				timeMs,
				retryCount,
				lastRetryReason
		);
	}
}
