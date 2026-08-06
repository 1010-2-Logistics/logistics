package com.logistics.user.presentation.controller;

import com.logistics.user.application.dto.command.ChangePasswordCommandDto;
import com.logistics.user.application.dto.command.UpdateMySlackIdCommandDto;
import com.logistics.user.application.dto.result.ChangeApprovalResultDto;
import com.logistics.user.application.dto.result.ChangePasswordResultDto;
import com.logistics.user.application.dto.result.UpdateMyInfoResultDto;
import com.logistics.user.application.facade.UserFacade;
import com.logistics.user.application.service.UserApprovalService;
import com.logistics.user.application.service.UserCommandService;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.global.exception.CustomException;
import com.logistics.user.global.exception.UserErrorCode;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.infrastructure.security.AuthenticatedUser;
import com.logistics.user.presentation.dto.request.UserApprovalRequestDto;
import com.logistics.user.presentation.dto.request.UserCreateRequestDto;
import com.logistics.user.presentation.dto.request.UserPasswordUpdateRequestDto;
import com.logistics.user.presentation.dto.request.UserUpdateRequestDto;
import com.logistics.user.presentation.dto.response.UserApprovalResponseDto;
import com.logistics.user.presentation.dto.response.UserPasswordUpdateResponseDto;
import com.logistics.user.presentation.dto.response.UserUpdateResponseDto;
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


    /**
     * 로그인한 사용자의 Slack ID를 수정한다.
     */
    @PatchMapping("/me")
    public ApiResponse<UserUpdateResponseDto> updateMyInfo(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        /*
         * Filter가 Authentication principal에 넣은
         * 현재 사용자 인증 정보를 꺼낸다.
         */
        AuthenticatedUser currentUser =
                (AuthenticatedUser) authentication.getPrincipal();

        /*
         * HTTP 요청값과 인증 사용자 ID를
         * Application Command로 변환한다.
         */
        UpdateMySlackIdCommandDto command =
                new UpdateMySlackIdCommandDto(
                        currentUser.userId(),
                        request.slackId()
                );

        UpdateMyInfoResultDto result =
                userCommandService.updateMySlackId(command);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "내 정보 수정 성공",
                UserUpdateResponseDto.from(result)
        );
    }

    /**
     * 로그인한 사용자의 비밀번호 변경
     */
    @PatchMapping("/me/password")
    public ApiResponse<UserPasswordUpdateResponseDto> changePassword(
            Authentication authentication,
            @Valid @RequestBody UserPasswordUpdateRequestDto request
    ) {
        /*
         * Gateway 헤더 기반 Filter가 principal에 넣은
         * 현재 로그인 사용자 정보 추출
         */
        AuthenticatedUser currentUser =
                (AuthenticatedUser) authentication.getPrincipal();

        ChangePasswordCommandDto command =
                new ChangePasswordCommandDto(
                        currentUser.userId(),
                        request.currentPassword(),
                        request.newPassword(),
                        request.newPasswordConfirm()
                );

        ChangePasswordResultDto result =
                userCommandService.changePassword(command);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "비밀번호 변경 성공",
                UserPasswordUpdateResponseDto.from(result)
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