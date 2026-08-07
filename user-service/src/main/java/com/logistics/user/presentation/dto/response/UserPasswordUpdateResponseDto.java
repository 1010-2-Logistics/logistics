package com.logistics.user.presentation.dto.response;

import com.logistics.user.application.dto.result.ChangePasswordResultDto;
import java.time.LocalDateTime;

/**
 * 비밀번호 변경 응답.
 */
public record UserPasswordUpdateResponseDto(
        Long userId
) {

    public static UserPasswordUpdateResponseDto from(
            ChangePasswordResultDto result
    ) {
        return new UserPasswordUpdateResponseDto(
                result.userId()
        );
    }
}