package com.logistics.user.application.dto.command;

import com.logistics.user.domain.entity.UserRole;

import java.util.UUID;

/**
 * 사용자 소속 및 권한 변경 입력값.
 */
public record ChangeUserAffiliationCommandDto(

        Long userId,

        UserRole role,

        UUID companyId,

        UUID hubId

) {
}