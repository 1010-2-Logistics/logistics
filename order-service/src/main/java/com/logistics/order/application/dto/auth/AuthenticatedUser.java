package com.logistics.order.application.dto.auth;

import com.logistics.order.domain.entity.Role;

import java.util.UUID;

public record AuthenticatedUser(
        Long userId,
        Role role,
        UUID hubId,
        UUID companyId
) {
}
