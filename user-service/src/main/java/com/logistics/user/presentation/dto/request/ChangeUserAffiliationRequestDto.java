package com.logistics.user.presentation.dto.request;

import com.logistics.user.application.dto.command.ChangeUserAffiliationCommandDto;
import com.logistics.user.domain.entity.UserRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * 내부 서비스에서 사용자 소속 및 권한 변경 요청값
 *
 * 요청시 변경하지 않을 값이라도 기존 값을 입력해주어야해요.
 * null을 입력하면 null이 저장됩니다.
 */
public record ChangeUserAffiliationRequestDto(

        @NotNull(message = "변경할 권한은 필수입니다.")
        UserRole role,

        UUID companyId,

        UUID hubId

) {

    /**
     * HTTP 요청 객체를 Application 계층의 Command로 변환한다.
     */
    public ChangeUserAffiliationCommandDto toCommand(
            Long userId
    ) {
        return new ChangeUserAffiliationCommandDto(
                userId,
                role,
                companyId,
                hubId
        );
    }
}