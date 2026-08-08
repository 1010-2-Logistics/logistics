package com.logistics.user.presentation.dto.response;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSummaryResponseDto(

        Long userId,
        String username,
        String slackId,
        UserStatus status,
        UserRole role,
        UUID hubId,
        UUID companyId,
        LocalDateTime createdAt

) {

    public static UserSummaryResponseDto from(User user) {

        return new UserSummaryResponseDto(

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