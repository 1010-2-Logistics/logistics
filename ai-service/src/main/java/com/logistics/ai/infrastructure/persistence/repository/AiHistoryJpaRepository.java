package com.logistics.ai.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.logistics.ai.domain.entity.AiHistory;

public interface AiHistoryJpaRepository extends JpaRepository<AiHistory, UUID> {

}
