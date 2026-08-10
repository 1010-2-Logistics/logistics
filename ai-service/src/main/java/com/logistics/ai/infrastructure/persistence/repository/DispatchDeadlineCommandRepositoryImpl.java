package com.logistics.ai.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import com.logistics.ai.domain.entity.AiHistory;
import com.logistics.ai.domain.repository.DispatchDeadlineCommandRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DispatchDeadlineCommandRepositoryImpl implements DispatchDeadlineCommandRepository {

	private final AiHistoryJpaRepository jpaRepository;

	@Override
	public AiHistory save(AiHistory entity) {
		return jpaRepository.save(entity);
	}
	
	
}
