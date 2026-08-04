package com.logistics.user.presentation.dto.response;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;

public record UserSummaryResponseDto(
        Long userId,
        String username,
        UserStatus status,
        UserRole role
) {

    public static UserSummaryResponseDto from(User user) {
        return new UserSummaryResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getStatus(),
                user.getRole()
        );
    }
}