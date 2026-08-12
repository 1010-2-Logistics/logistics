package com.logistics.slack.application.dto.auth;


import com.logistics.slack.domain.entity.Role;

import java.util.UUID;

public record AuthenticatedUser(
        Long userId,
        Role role,
        UUID hubId,
        UUID companyId
) {
}
