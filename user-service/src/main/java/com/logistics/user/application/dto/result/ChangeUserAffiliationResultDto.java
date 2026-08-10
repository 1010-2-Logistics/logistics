package com.logistics.user.application.dto.result;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 소속 및 권한 변경 결과.
 */
public record ChangeUserAffiliationResultDto(

        Long userId,

        UserRole role,

        UUID companyId,

        UUID hubId,

        LocalDateTime updatedAt

) {

    public static ChangeUserAffiliationResultDto from(
            User user
    ) {
        return new ChangeUserAffiliationResultDto(
                user.getUserId(),
                user.getRole(),
                user.getCompanyId(),
                user.getHubId(),
                user.getUpdatedAt()
        );
    }

}