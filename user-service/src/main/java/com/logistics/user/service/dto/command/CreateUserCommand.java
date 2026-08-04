package com.logistics.user.service.dto.command;

import com.logistics.user.domain.entity.UserRole;

import java.util.UUID;


public record CreateUserCommand(
        String username,
        String encodedPassword,
        String slackId,
        UserRole role,
        UUID companyId,
        UUID hubId
) {
}
