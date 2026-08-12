package com.logistics.ai.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.ai.domain.entity.AiStatus;
import com.logistics.ai.infrastructure.persistence.repository.AiHistoryJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchDeadlineQueryService {

	private final AiHistoryJpaRepository jpaRepository;

	public boolean hasSucceeded(UUID orderId) {
		return jpaRepository.existsByOrderIdAndStatus(orderId, AiStatus.SUCCESS);
	}
}
