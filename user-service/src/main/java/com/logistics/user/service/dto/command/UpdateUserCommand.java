package com.logistics.user.service.dto.command;

public record UpdateUserCommand(
        Long userId,
        String slackId
) {
}