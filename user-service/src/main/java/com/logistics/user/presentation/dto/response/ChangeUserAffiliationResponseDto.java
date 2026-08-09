package com.logistics.user.presentation.dto.response;

import com.logistics.user.application.dto.result.ChangeUserAffiliationResultDto;
import com.logistics.user.domain.entity.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 소속 및 권한 변경 응답 DTO.
 */
public record ChangeUserAffiliationResponseDto(

        Long userId,

        UserRole role,

        UUID companyId,

        UUID hubId,

        LocalDateTime updatedAt

) {

    public static ChangeUserAffiliationResponseDto from(
            ChangeUserAffiliationResultDto result
    ) {

        return new ChangeUserAffiliationResponseDto(
                result.userId(),
                result.role(),
                result.companyId(),
                result.hubId(),
                result.updatedAt()
        );
    }

}