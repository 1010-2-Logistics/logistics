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
public class SlackQueryRepositoryImpl implements SlackQueryRepository {

    private final SlackJpaRepository slackJpaRepository;

    @Override
    public Optional<Slack> findByIdAndDeletedAtIsNull(UUID slackMessageId) {
        return slackJpaRepository.findBySlackMessageIdAndDeletedAtIsNull(slackMessageId);
    }

    @Override
    public Page<Slack> search(
            SlackStatus status,
            Long senderId,
            Long receiverId,
            UUID referenceId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        return slackJpaRepository.search(
                status,
                senderId,
                receiverId,
                referenceId,
                createdFrom,
                createdTo,
                pageable
        );
    }
}
