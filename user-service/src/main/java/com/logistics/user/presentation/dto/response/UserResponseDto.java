package com.logistics.user.presentation.dto.response;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import java.util.UUID;

public record UserResponseDto(
        Long userId,
        String username,
        String slackId,
        UserStatus status,
        UserRole role,
        UUID companyId,
        UUID hubId
) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getSlackId(),
                user.getStatus(),
                user.getRole(),
                user.getCompanyId(),
                user.getHubId()
        );
    }
}