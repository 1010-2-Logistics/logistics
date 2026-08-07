package com.logistics.user.application.dto.command;

/**
 * 내 비밀번호 변경 입력값.
 */
public record ChangePasswordCommandDto(
        Long userId,
        String currentPassword,
        String newPassword,
        String newPasswordConfirm
) {
}