package com.logistics.slack.application.dto.result;

import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackCreateResult(
        UUID slackMessageId,
        UUID senderId,
        UUID receiverId,
        String message,
        SlackStatus status,
        Integer retryCount,
        UUID referenceId,
        LocalDateTime createdAt
) {
    public static SlackCreateResult from(
            Slack slack
    ) {
        return new SlackCreateResult(
                slack.getSlackMessageId(),
                slack.getSenderId(),
                slack.getReceiverId(),
                slack.getMessage(),
                slack.getStatus(),
                slack.getRetryCount(),
                slack.getReferenceId(),
                slack.getCreatedAt()
        );
    }
}
