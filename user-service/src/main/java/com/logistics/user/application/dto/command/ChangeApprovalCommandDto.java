package com.logistics.user.application.dto.command;

import com.logistics.user.domain.entity.ApprovalDecision;

public record ChangeApprovalCommandDto(

        /**
         * 승인 또는 거절할 사용자 ID.
         */
        Long targetUserId,

        /**
         * 요청을 수행한 관리자 ID.
         */
        Long processedBy,

        /**
         * APPROVE 또는 REJECT.
         */
        ApprovalDecision decision,

        String rejectionReason

) {
}