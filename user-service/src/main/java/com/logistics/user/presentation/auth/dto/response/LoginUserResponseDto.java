package com.logistics.user.presentation.auth.dto.response;

import com.logistics.user.application.dto.result.LoginUserResultDto;
import com.logistics.user.domain.entity.UserRole;
import java.util.UUID;

/**
 * 로그인 응답에 포함되는 사용자 정보.
 */
public record LoginUserResponseDto(
        Long userId,
        String username,
        UserRole role,
        UUID hubId,
        UUID companyId
) {

    public static LoginUserResponseDto from(
            LoginUserResultDto result
    ) {
        return new LoginUserResponseDto(
                result.userId(),
                result.username(),
                result.role(),
                result.hubId(),
                result.companyId()
        );
    }
}