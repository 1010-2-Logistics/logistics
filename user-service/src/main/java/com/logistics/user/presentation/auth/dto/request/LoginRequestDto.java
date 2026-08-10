package com.logistics.user.presentation.auth.dto.request;

import com.logistics.user.application.dto.command.LoginCommandDto;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 HTTP 요청 DTO.
 *
 * 역할:
 * - 클라이언트의 JSON 요청 수신
 * - username, password 필수값 검증
 * - Application 계층의 Command로 변환
 */
public record LoginRequestDto(

        @NotBlank(message = "username은 필수입니다.")
        String username,

        @NotBlank(message = "password는 필수입니다.")
        String password
) {

    public LoginCommandDto toCommand() {
        return new LoginCommandDto(
                username,
                password
        );
    }
}