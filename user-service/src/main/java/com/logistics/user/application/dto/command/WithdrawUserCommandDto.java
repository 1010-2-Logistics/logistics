package com.logistics.user.application.dto.command;

/**
 * 회원 탈퇴입력값.
 */
public record WithdrawUserCommandDto(
        Long userId, //헤더에서
        String password //사용자가 입력
) {
}