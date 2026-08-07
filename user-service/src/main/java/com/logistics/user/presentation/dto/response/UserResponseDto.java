package com.logistics.user.presentation.dto.response;

import com.logistics.user.application.dto.result.UserDetailResultDto;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
        Long userId,
        String username,
        String slackId,
        UserStatus status,
        UserRole role,
        UUID hubId,
        UUID companyId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static UserResponseDto from(
            UserDetailResultDto result
    ) {
        return new UserResponseDto(
                result.userId(),
                result.username(),
                result.slackId(),
                result.status(),
                result.role(),
                result.hubId(),
                result.companyId(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}