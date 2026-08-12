package com.logistics.slack.application.dto.command;

import com.logistics.slack.presentation.dto.request.SlackCreateRequestDto;

import java.util.UUID;

public record SlackCreateCommand(
        Long senderId,
        Long receiverId,
        String message,
        UUID referenceId
) {
    public static SlackCreateCommand toCommand(
            Long senderId,
            SlackCreateRequestDto slackCreateRequestDto
    ) {
        return new SlackCreateCommand(
                senderId,
                slackCreateRequestDto.receiverId(),
                slackCreateRequestDto.message(),
                slackCreateRequestDto.referenceId()
        );
    }
}
