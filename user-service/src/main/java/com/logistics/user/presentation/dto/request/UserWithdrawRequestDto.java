package com.logistics.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 회원 탈퇴 시 본인 확인 비밀번호 요청
 */
public record UserWithdrawRequestDto(
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}