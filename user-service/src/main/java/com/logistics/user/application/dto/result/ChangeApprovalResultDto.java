package com.logistics.user.application.dto.result;

import com.logistics.user.domain.entity.UserStatus;
import java.time.LocalDateTime;

/**
 * 사용자 승인 상태 변경 처리 결과.
 */
public record ChangeApprovalResultDto(
        Long userId,
        UserStatus previousStatus,
        UserStatus status,
        Long processedBy,
        LocalDateTime processedAt

) {
}