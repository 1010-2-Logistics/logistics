package com.logistics.user.presentation.auth.dto.response;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserStatus;

/**
 * 회원가입 성공 응답.
 *
 * password, Slack ID처럼 민감한 값은 제외
 */
public record SignupResponseDto(
        Long userId,
        String username,
        UserStatus status
) {

    public static SignupResponseDto from(User user) {
        return new SignupResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getStatus()
        );
    }
}