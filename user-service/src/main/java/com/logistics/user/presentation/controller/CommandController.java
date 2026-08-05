package com.logistics.user.presentation.controller;

import com.logistics.user.application.dto.command.UpdateUserCommandDto;
import com.logistics.user.application.dto.result.ChangeApprovalResultDto;
import com.logistics.user.application.facade.UserFacade;
import com.logistics.user.application.service.UserApprovalService;
import com.logistics.user.application.service.UserCommandService;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.presentation.dto.request.UserApprovalRequestDto;
import com.logistics.user.presentation.dto.request.UserCreateRequestDto;
import com.logistics.user.presentation.dto.request.UserUpdateRequestDto;
import com.logistics.user.presentation.dto.response.UserApprovalResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class CommandController {

    private final UserFacade userFacade;
    private final UserCommandService userCommandService;
    private final UserApprovalService userApprovalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(
            @Valid @RequestBody UserCreateRequestDto request
    ) {
        Long userId = userFacade.createUser(request.toCommand());

        return ApiResponse.success(
                201,
                "사용자 생성 성공",
                userId
        );
    }

    @PutMapping("/{userId}")
    public ApiResponse<Void> update(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        // HTTP 요청 객체를 애플리케이션 계층의 Command로 변환한다.
        UpdateUserCommandDto command = new UpdateUserCommandDto(
                userId,
                request.slackId()
        );

        // 실제 사용자 수정 비즈니스 로직을 실행한다.
        userCommandService.update(command);

        return ApiResponse.success(
                200,
                "사용자 수정 성공",
                null
        );
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long userId
    ) {
        // TODO: 인증 적용 후 JWT에서 실제 로그인 사용자 ID를 추출한다.
        Long deletedBy = 1L;

        userCommandService.delete(
                userId,
                deletedBy
        );
    }


    /**
     * PENDING 상태의 가입 신청을 승인 또는 거절
     */
    @PatchMapping("/{userId}/approval")
    public ApiResponse<UserApprovalResponseDto> changeApproval(
            @PathVariable Long userId,
            @Valid @RequestBody UserApprovalRequestDto request,
            Authentication authentication
    ) {

        Long processedBy =
                extractAuthenticatedUserId(authentication);

        ChangeApprovalResultDto result =
                userApprovalService.changeApproval(
                        request.toCommand(
                                userId,
                                processedBy
                        )
                );

        String message =
                result.status() == UserStatus.APPROVED
                        ? "사용자 가입 승인 성공"
                        : "사용자 가입 거절 성공";

        return ApiResponse.success(
                HttpStatus.OK.value(),
                message,
                UserApprovalResponseDto.from(result)
        );
    }

    private Long extractAuthenticatedUserId(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new CustomException(
                    UserErrorCode.AUTH_UNAUTHORIZED
            );
        }

        try {
            return Long.valueOf(
                    authentication.getName()
            );
        } catch (NumberFormatException exception) {
            throw new CustomException(
                    UserErrorCode.AUTH_UNAUTHORIZED
            );
        }
    }
}