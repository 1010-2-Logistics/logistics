package com.logistics.user.application.dto.result;

public record LoginResultDto(
        String grantType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        LoginUserResultDto user
) {
}