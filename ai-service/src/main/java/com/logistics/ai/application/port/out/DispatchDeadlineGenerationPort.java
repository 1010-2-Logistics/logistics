package com.logistics.ai.application.port.out;

import com.logistics.ai.application.dto.result.DispatchDeadlineResultDto;

public interface DispatchDeadlineGenerationPort {
	DispatchDeadlineResultDto generate(String requestPrompt, String aiModel);
}
