package com.logistics.ai.application.dto.result;

public record DlqRedriveResult(
		int requestedCount,
		int redrivenCount,
		int skippedCount,
		int remainingMessageCount
) {
}
