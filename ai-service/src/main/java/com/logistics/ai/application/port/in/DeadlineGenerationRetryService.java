package com.logistics.ai.application.port.in;

import com.logistics.ai.application.dto.result.DispatchDeadlineRetryResultDto;

public interface DeadlineGenerationRetryService {
	DispatchDeadlineRetryResultDto generate(String requestPrompt, String aiModel);
}
