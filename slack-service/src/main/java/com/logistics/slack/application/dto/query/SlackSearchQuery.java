package com.logistics.slack.application.dto.query;

import com.logistics.slack.domain.entity.SlackStatus;
import com.logistics.slack.presentation.dto.request.SlackSearchRequestDto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackSearchQuery(
        SlackStatus status,
        Long senderId,
        Long receiverId,
        UUID referenceId,

        LocalDateTime createdFrom,
        LocalDateTime createdTo,

        String sort,
        Integer page,
        Integer size

) {
    public static SlackSearchQuery from(
            SlackSearchRequestDto slackSearchRequestDto
    ) {
        return new SlackSearchQuery(
                slackSearchRequestDto.status(),
                slackSearchRequestDto.senderId(),
                slackSearchRequestDto.receiverId(),
                slackSearchRequestDto.referenceId(),
                slackSearchRequestDto.createdFrom(),
                slackSearchRequestDto.createdTo(),
                slackSearchRequestDto.sort(),
                slackSearchRequestDto.page(),
                slackSearchRequestDto.size()
        );
    }
}
