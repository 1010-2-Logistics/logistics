package com.logistics.user.application.dto.query;

import com.logistics.user.domain.entity.UserRole;
import java.util.UUID;

// 관리자에 의한, 사용자 상세 정보 조회를 위한 입력값
public record GetUserDetailQueryDto(
        Long requesterId,
        UserRole requesterRole,
        UUID requesterHubId,
        Long targetUserId
) {
}