package com.logistics.user.presentation.auth.dto.response;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 회원가입 성공 응답.
 *
 * password, Slack ID처럼 민감한 값은 제외
 */
public record SignupResponseDto(
        Long userId,
        String username,
        String slackId,
        UserStatus status,
        UserRole role,
        UUID hubId,
        UUID companyId,
        LocalDateTime createdAt
) {

    public static SignupResponseDto from(User user) {
        return new SignupResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getSlackId(),
                user.getStatus(),
                user.getRole(),
                user.getHubId(),
                user.getCompanyId(),
                user.getCreatedAt()
        );
    }
}