package com.logistics.user.presentation.dto.response;

import com.logistics.user.application.dto.result.ChangeApprovalResult;
import com.logistics.user.domain.entity.UserStatus;
import java.time.LocalDateTime;

/**
 * 사용자 가입 승인 상태 변경 응답 DTO.
 */
public record UserApprovalResponseDto(

        Long userId,
        UserStatus previousStatus,
        UserStatus status,
        Long processedBy,
        LocalDateTime processedAt

) {

    public static UserApprovalResponseDto from(
            ChangeApprovalResult result
    ) {
        return new UserApprovalResponseDto(
                result.userId(),
                result.previousStatus(),
                result.status(),
                result.processedBy(),
                result.processedAt()
        );
    }
}