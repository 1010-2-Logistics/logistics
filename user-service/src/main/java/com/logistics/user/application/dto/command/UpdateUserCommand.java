package com.logistics.user.application.dto.command;

public record UpdateUserCommand(
        Long userId,
        String slackId
) {
}