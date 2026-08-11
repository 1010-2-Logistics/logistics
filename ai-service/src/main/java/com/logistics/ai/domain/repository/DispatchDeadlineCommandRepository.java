package com.logistics.ai.domain.repository;

import com.logistics.ai.domain.entity.AiHistory;

public interface DispatchDeadlineCommandRepository {

	AiHistory save(AiHistory entity);
}
