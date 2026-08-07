package com.logistics.user.presentation.auth.dto.response;

import com.logistics.user.application.dto.result.LoginResultDto;

/**
 * 로그인 성공 HTTP 응답 DTO.
 */
public record LoginResponseDto(
        String grantType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        LoginUserResponseDto user
) {

    public static LoginResponseDto from(
            LoginResultDto result
    ) {
        return new LoginResponseDto(
                result.grantType(),
                result.accessToken(),
                result.refreshToken(),
                result.accessTokenExpiresIn(),
                result.refreshTokenExpiresIn(),
                LoginUserResponseDto.from(result.user())
        );
    }
}