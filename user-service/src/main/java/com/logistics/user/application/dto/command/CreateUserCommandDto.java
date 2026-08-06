package com.logistics.user.application.dto.command;

import com.logistics.user.domain.entity.UserRole;

import java.util.UUID;


public record CreateUserCommandDto(
        String username,
        String encodedPassword,
        String slackId,
        UserRole role,
        UUID companyId,
        UUID hubId
) {
}
