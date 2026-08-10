package com.logistics.slack.application.dto.result;

import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackListResult(
        UUID slackMessageId,
        Long senderId,
        Long receiverId,
        String message,
        SlackStatus status,
        Integer retryCount,
        LocalDateTime sentAt,
        UUID referenceId,
        LocalDateTime createdAt
) {
    public static SlackListResult from(Slack slack) {
        return new SlackListResult(
                slack.getSlackMessageId(),
                slack.getSenderId(),
                slack.getReceiverId(),
                slack.getMessage(),
                slack.getStatus(),
                slack.getRetryCount(),
                slack.getSentAt(),
                slack.getReferenceId(),
                slack.getCreatedAt()
        );
    }
}
