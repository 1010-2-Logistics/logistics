package com.logistics.slack.application.dto.command;

import com.logistics.slack.presentation.dto.request.SlackCreateRequestDto;

import java.util.UUID;

public record SlackCreateCommand(
        String receiverId,
        String message,
        UUID referenceId
) {
    public static SlackCreateCommand toCommand(
            SlackCreateRequestDto slackCreateRequestDto
    ) {
        return new SlackCreateCommand(
                slackCreateRequestDto.receiverId(),
                slackCreateRequestDto.message(),
                slackCreateRequestDto.referenceId()
        );
    }
}
