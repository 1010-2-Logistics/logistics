package com.logistics.slack.domain.repository;

import com.logistics.slack.domain.entity.Slack;
import java.util.Optional;
import java.util.UUID;

public interface SlackCommandRepository {

    Slack save(Slack sample);

    Optional<Slack> findByIdAndDeletedAtIsNull(UUID sampleId);
}
