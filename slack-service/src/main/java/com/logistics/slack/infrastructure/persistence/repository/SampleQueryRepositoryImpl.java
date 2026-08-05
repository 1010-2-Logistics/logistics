package com.logistics.slack.infrastructure.persistence.repository;

import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.entity.SlackStatus;
import com.logistics.slack.domain.repository.SlackQueryRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SampleQueryRepositoryImpl implements SlackQueryRepository {

    private final SampleJpaRepository jpaRepository;

    @Override
    public Optional<Slack> findByIdAndDeletedAtIsNull(UUID sampleId) {
        return jpaRepository.findBySampleIdAndDeletedAtIsNull(sampleId);
    }

    @Override
    public Page<Slack> search(
            SlackStatus status,
            UUID senderId,
            UUID receiverId,
            UUID referenceId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        return null;
    }
}
