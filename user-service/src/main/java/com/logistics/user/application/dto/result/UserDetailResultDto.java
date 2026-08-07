package com.logistics.user.application.dto.result;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application 계층에서 조회 결과 전달 객체.
 */
public record UserDetailResultDto(
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

    public static UserDetailResultDto from(User user) {
        return new UserDetailResultDto(
                user.getUserId(),
                user.getUsername(),
                user.getSlackId(),
                user.getStatus(),
                user.getRole(),
                user.getHubId(),
                user.getCompanyId(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}