package com.logistics.slack.domain.repository;

import com.logistics.slack.domain.entity.Slack;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.logistics.slack.domain.entity.SlackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SlackQueryRepository {

    Optional<Slack> findByIdAndDeletedAtIsNull(UUID slackMessageId);

    Page<Slack> search(
            SlackStatus status,
            Long senderId,
            Long receiverId,
            UUID referenceId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    );
}
