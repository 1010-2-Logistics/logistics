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

    private final SlackJpaRepository jpaRepository;
    private final SlackJpaRepository slackJpaRepository;

    @Override
    public Slack save(Slack slack) {
        return jpaRepository.save(slack);
    }

    @Override
    public Optional<Slack> findById(UUID slackMessageId) {
        return slackJpaRepository.findById(slackMessageId);
    }

    @Override
    public Optional<Slack> findByIdAndDeletedAtIsNull(UUID slackMessageId) {
        return jpaRepository.findBySlackMessageIdAndDeletedAtIsNull(slackMessageId);
    }
}
