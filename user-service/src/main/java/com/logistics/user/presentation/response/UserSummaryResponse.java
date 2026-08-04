package com.logistics.user.presentation.response;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;

public record UserSummaryResponse(
        Long userId,
        String username,
        UserStatus status,
        UserRole role
) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getUserId(),
                user.getUsername(),
                user.getStatus(),
                user.getRole()
        );
    }
}