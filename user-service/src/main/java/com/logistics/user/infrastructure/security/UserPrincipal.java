package com.logistics.user.infrastructure.security;

import com.logistics.user.domain.entity.UserRole;
import java.util.UUID;

/**
 * Gateway가 전달한 인증 정보
 */
public record UserPrincipal(
        Long userId,
        UserRole role,
        UUID hubId,
        UUID companyId
) {

    public void validateRoleConstraints() {

        if (role == UserRole.MASTER
                && (hubId != null || companyId != null)) {
            throw new IllegalArgumentException(
                    "MASTER는 소속 정보를 가질 수 없습니다."
            );
        }

        if (role == UserRole.HUB_MANAGER
                && (hubId == null || companyId != null)) {
            throw new IllegalArgumentException(
                    "HUB_MANAGER는 hubId가 필요합니다."
            );
        }

        if ((role == UserRole.COMPANY_MANAGER)
                && (hubId == null || companyId == null)) {
            throw new IllegalArgumentException(
                    "업체 역할은 hubId와 companyId가 필요합니다."
            );
        }
    }
}