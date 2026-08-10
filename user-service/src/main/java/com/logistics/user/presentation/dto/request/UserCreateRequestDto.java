package com.logistics.user.presentation.dto.request;

import com.logistics.user.application.dto.command.CreateUserCommandDto;
import com.logistics.user.domain.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * 사용자 생성 요청 DTO.
 * 현재 구조에서는 password가 아직 암호화하지 않음.
 * 실제 회원가입 구현 시 Service 또는 Facade에서 암호화
 */
public record UserCreateRequestDto(

        @NotBlank(message = "username은 필수입니다.")
        @Size(min = 4, max = 10, message = "username은 4자 이상 10자 이하여야 합니다.")
        @Pattern(
                regexp = "^[a-z0-9]+$",
                message = "username은 영문 소문자와 숫자만 사용할 수 있습니다."
        )
        String username,

        @NotBlank(message="name은 필수입니다. 실명을 작성해주세요.")
        @Size(max = 50, message = "name은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "password는 필수입니다.")
        String password,

        @NotBlank(message = "slackId는 필수입니다.")
        String slackId,

        @NotNull(message = "role은 필수입니다.")
        UserRole role,

        UUID companyId,

        UUID hubId

) {

    public CreateUserCommandDto toCommand() {
        return new CreateUserCommandDto(
                username,
                name,
                password,
                slackId,
                role,
                companyId,
                hubId
        );
    }
}