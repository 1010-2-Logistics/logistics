package com.logistics.inventory.application.dto.auth;

import com.logistics.inventory.domain.entity.Role;

import java.util.UUID;

public record AuthenticatedUser(
        Long userId,
        Role role,
        UUID hubId,
        UUID companyId
) {
}
