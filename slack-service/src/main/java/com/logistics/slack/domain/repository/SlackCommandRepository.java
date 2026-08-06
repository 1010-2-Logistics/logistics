package com.logistics.slack.domain.repository;

import com.logistics.slack.domain.entity.Slack;

import java.util.Optional;
import java.util.UUID;

public interface SlackCommandRepository {
    Slack save(Slack slack);

    Optional<Slack> findById(UUID slackMessageId);

    Optional<Slack> findByIdAndDeletedAtIsNull(UUID sampleId);
}
