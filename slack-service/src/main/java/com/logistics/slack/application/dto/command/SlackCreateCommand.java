package com.logistics.slack.application.dto.command;

import com.logistics.slack.presentation.dto.request.SlackCreateRequestDto;

import java.util.UUID;

public record SlackCreateCommand(
        Long senderId,
        Long receiverId,
        String message,
        Long referenceId
        // TODO : 중복 요청 방지 키
//        String idempotencyKey
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
