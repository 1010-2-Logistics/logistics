package com.logistics.slack.presentation.dto.response;

import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackRetryResponseDto(
        UUID slackMessageId,
        SlackStatus status,
        Integer retryCount,
        String errorMessage,
        LocalDateTime updatedAt
) {
}
