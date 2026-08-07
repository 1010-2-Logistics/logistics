package com.logistics.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 로그인한 사용자의 비밀번호 변경 요청.
 */
public record UserPasswordUpdateRequestDto(

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        String currentPassword,

        /**
         * (?=.*[a-z])          소문자 포함
         * (?=.*[A-Z])          대문자 포함
         * (?=.*\d)             숫자 포함
         * (?=.*[^a-zA-Z0-9])   특수문자 포함
         * .{8,15}              전체 길이 8~15자
         */
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,15}$",
                message = "비밀번호는 8~15자이며 영문 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
        )
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인 값은 필수입니다.")
        String newPasswordConfirm
) {
}