package com.logistics.ai.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.ai.application.port.in.DispatchDeadlineCommandService;
import com.logistics.ai.domain.entity.AiHistory;
import com.logistics.ai.domain.repository.DispatchDeadlineCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DispatchDeadlineCommandServiceImpl implements DispatchDeadlineCommandService {

	private final DispatchDeadlineCommandRepository commandRepository;

	@Override
	@Transactional
	public AiHistory saveSucceeded(AiHistory successAiHistory) {
		return commandRepository.save(successAiHistory);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public AiHistory saveFailed(
			UUID orderId,
			UUID deliveryId,
			String requestPrompt,
			String aiModel,
			String errorMessage,
			int retryCount
	) {
		
		AiHistory failedAiHistory = AiHistory.failed(
				orderId,
				deliveryId,
				requestPrompt,
				aiModel, 
				errorMessage,
				retryCount
		);
		
		return commandRepository.save(failedAiHistory);
	}
	
	
	
}
