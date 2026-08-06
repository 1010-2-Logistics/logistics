package com.logistics.user.application.dto.command;

public record UpdateUserCommandDto(
        Long userId,
        String slackId
) {
}