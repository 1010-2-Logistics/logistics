package com.logistics.slack.application.dto.result;

import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackCreatResult(
        UUID slackMessageId,
        String senderId,
        String receiverId,
        String message,
        SlackStatus status,
        Integer retryCount,
        UUID referenceId,
        LocalDateTime createdAt
) {
}
