package com.logistics.slack.infrastructure.persistence.repository;

import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackCommandRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SlackCommandRepositoryImpl implements SlackCommandRepository {

    private final SampleJpaRepository jpaRepository;

    @Override
    public Slack save(Slack sample) {
        return jpaRepository.save(sample);
    }

    @Override
    public Optional<Slack> findByIdAndDeletedAtIsNull(UUID sampleId) {
        return jpaRepository.findBySampleIdAndDeletedAtIsNull(sampleId);
    }
}
