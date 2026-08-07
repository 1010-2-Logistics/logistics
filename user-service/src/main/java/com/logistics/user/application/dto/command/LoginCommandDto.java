package com.logistics.user.application.dto.command;

/**
 * 로그인 필수 입력값
 */
public record LoginCommandDto(
        String username,
        String password
) {
}