package com.logistics.user.application.dto.command;

import com.logistics.user.domain.entity.UserRole;
import java.util.UUID;

/**
 * 회원가입 유스케이스에 전달되는 입력값.
 *
 * presentation 계층의 SignupRequest가
 * application 계층에 직접 노출되지 않도록 분리한다.
 */
public record SignupCommandDto(
        String username,
        String name,
        String password,
        String passwordConfirm,
        String slackId,
        UserRole role,
        UUID companyId,
        UUID hubId
) {
}