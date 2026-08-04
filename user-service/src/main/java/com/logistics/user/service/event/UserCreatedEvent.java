package com.logistics.user.service.event;

import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;

public record UserCreatedEvent(
        Long userId,
        String username,
        UserRole role,
        UserStatus status
) {
}