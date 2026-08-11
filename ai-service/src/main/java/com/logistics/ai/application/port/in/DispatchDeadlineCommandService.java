package com.logistics.ai.application.port.in;

import java.util.UUID;

import com.logistics.ai.domain.entity.AiHistory;

public interface DispatchDeadlineCommandService {

	AiHistory saveSucceeded(AiHistory successAiHistory);
	
	AiHistory saveFailed(UUID orderId, UUID deliveryId, String requestPrompt, String aiModel, String errorMessage, int retryCount);
}
