package com.logistics.user.presentation.dto.request;

import com.logistics.user.application.dto.command.ChangeApprovalCommandDto;
import com.logistics.user.domain.entity.ApprovalDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 사용자 가입 승인·거절 HTTP 요청 DTO.
 */
public record UserApprovalRequestDto(

        /**
         * 가입 처리 결정.
         *
         * 가능한 값:
         * - APPROVE
         * - REJECT
         */
        @NotNull(message = "가입 처리 결과는 필수입니다.")
        ApprovalDecision decision,

        @Size(
                max = 500,
                message = "거절 사유는 최대 500자까지 입력할 수 있습니다."
        )
                String rejectionReason

) {

    /**
     * HTTP 요청 객체를 Application 계층의 Command로 변환한다.
     *
     * @param targetUserId 승인 대상 사용자 ID
     * @param processedBy 요청을 수행한 관리자 ID
     */
    public ChangeApprovalCommandDto toCommand(
            Long targetUserId,
            Long processedBy
    ) {
        return new ChangeApprovalCommandDto(
                targetUserId,
                processedBy,
                decision,
                rejectionReason
        );
    }
}