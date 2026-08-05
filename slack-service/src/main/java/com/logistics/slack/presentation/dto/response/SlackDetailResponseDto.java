package com.logistics.slack.presentation.dto.response;

import com.logistics.slack.application.dto.result.SlackDetailResult;
import com.logistics.slack.domain.entity.SlackStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackDetailResponseDto(
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
    public static SlackDetailResponseDto from(
            SlackDetailResult slackDetailResult
    ) {
        return new SlackDetailResponseDto(
                slackDetailResult.slackMessageId(),
                slackDetailResult.senderId(),
                slackDetailResult.receiverId(),
                slackDetailResult.message(),
                slackDetailResult.status(),
                slackDetailResult.errorMessage(),
                slackDetailResult.retryCount(),
                slackDetailResult.sentAt(),
                slackDetailResult.referenceId(),
                slackDetailResult.createdAt(),
                slackDetailResult.updatedAt()
        );
    }
}
