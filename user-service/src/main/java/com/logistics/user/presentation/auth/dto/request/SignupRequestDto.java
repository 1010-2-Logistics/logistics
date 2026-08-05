package com.logistics.user.presentation.auth.dto.request;

import com.logistics.user.application.dto.command.SignupCommand;
import com.logistics.user.domain.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 회원가입 요청 DTO.
 *
 * HTTP 요청 형식과 단순 입력 규칙을 검증한다.
 * username 중복, Slack ID 중복과 같은 DB 기반 검증은
 * Application Service에서 수행한다.
 */
public record SignupRequestDto(

        /**
         * 로그인 아이디.
         *
         * 조건:
         * - 4자 이상 10자 이하
         * - 영문 소문자와 숫자만 사용
         */
        @NotBlank(message = "username은 필수입니다.")
        @Size(
                min = 4,
                max = 10,
                message = "username은 4자 이상 10자 이하여야 합니다."
        )
        @Pattern(
                regexp = "^[a-z0-9]+$",
                message = "username은 영문 소문자와 숫자만 사용할 수 있습니다."
        )
        String username,

        /**
         * 평문 비밀번호.
         *
         * Entity에 전달하기 전에 암호화
         */
        @NotBlank(message = "password는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)"
                        + "(?=.*[!@#$%^&*()_+\\-=]).{8,15}$",
                message = "password는 8~15자이며 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
        )
        String password,

        /**
         * 사용자가 비밀번호를 잘못 입력했는지 확인하기 위함
         *
         * DB에는 저장하지 않는다.
         */
        @NotBlank(message = "passwordConfirm은 필수입니다.")
        String passwordConfirm,

        @NotBlank(message = "slackId는 필수입니다.")
        String slackId,

        /**
         * MASTER는 선택할 수 없는 검증 구현 예정.
         */
        @NotNull(message = "role은 필수입니다.")
        UserRole role,

        UUID companyId,

        UUID hubId
) {
        public SignupCommand toCommand() {
                return new SignupCommand(
                        username,
                        password,
                        passwordConfirm,
                        slackId,
                        role,
                        companyId,
                        hubId
                );
        }
}
