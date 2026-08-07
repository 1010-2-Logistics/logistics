package com.logistics.user.application.dto.result;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import java.util.UUID;

/**
 * 로그인 성공 응답에 포함할 사용자 정보.
 *
 * password, slackId 등 로그인 응답에 필요하지 않은 정보는 제외한다.
 */
public record LoginUserResultDto(
        Long userId,
        String username,
        UserRole role,
        UUID hubId,
        UUID companyId
) {

    public static LoginUserResultDto from(
            User user
    ) {
        return new LoginUserResultDto(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getHubId(),
                user.getCompanyId()
        );
    }
}