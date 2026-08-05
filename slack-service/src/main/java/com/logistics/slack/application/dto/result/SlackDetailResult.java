package com.logistics.slack.application.dto.result;

import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackDetailResult(
        UUID slackMessageId,
        UUID senderId,
        UUID receiverId,
        String message,
        SlackStatus status,
        String errorMessage,
        Integer retryCount,
        LocalDateTime sentAt,
        UUID referenceId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SlackDetailResult from(Slack slack) {
        return new SlackDetailResult(
                slack.getSlackMessageId(),
                slack.getSenderId(),
                slack.getReceiverId(),
                slack.getMessage(),
                slack.getStatus(),
                slack.getErrorMessage(),
                slack.getRetryCount(),
                slack.getSentAt(),
                slack.getReferenceId(),
                slack.getCreatedAt(),
                slack.getUpdatedAt()
        );
    }
}
