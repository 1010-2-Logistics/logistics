package com.logistics.user.infrastructure.security;

import com.logistics.user.domain.entity.UserRole;
import java.util.UUID;

/**
 * Gateway가 전달한 인증 정보
 */
public record AuthenticatedUser(
        Long userId,
        UserRole role,
        UUID hubId,
        UUID companyId
) {
}