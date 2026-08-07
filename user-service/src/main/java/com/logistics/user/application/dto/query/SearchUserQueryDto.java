package com.logistics.user.application.dto.query;

import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public record SearchUserQueryDto(

        // 인증 요청자 정보
        UserRole requesterRole,
        UUID requesterHubId,

        // 검색 조건
        String username,
        UserStatus status,
        UserRole role,
        UUID hubId,
        UUID companyId,

        // 페이징/정렬 정보
        Pageable pageable
) {
}