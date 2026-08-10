package com.logistics.user.presentation.controller;


import com.logistics.user.application.dto.command.ChangePasswordCommandDto;
import com.logistics.user.application.dto.command.DeleteUserCommandDto;
import com.logistics.user.application.dto.command.UpdateMySlackIdCommandDto;
import com.logistics.user.application.dto.command.WithdrawUserCommandDto;
import com.logistics.user.application.dto.result.ChangeApprovalResultDto;
import com.logistics.user.application.dto.result.ChangePasswordResultDto;
import com.logistics.user.application.dto.result.UpdateMyInfoResultDto;
import com.logistics.user.application.facade.UserFacade;
import com.logistics.user.application.service.UserApprovalService;
import com.logistics.user.application.service.UserCommandService;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.infrastructure.security.AuthenticatedUser;
import com.logistics.user.presentation.dto.request.*;
import com.logistics.user.presentation.dto.response.UserApprovalResponseDto;
import com.logistics.user.presentation.dto.response.UserPasswordUpdateResponseDto;
import com.logistics.user.presentation.dto.response.UserUpdateResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "User API"
)
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class CommandController {

    private final UserFacade userFacade;
    private final UserCommandService userCommandService;
    private final UserApprovalService userApprovalService;

    @Operation(
            summary = "사용자 생성",
            description = """
                새로운 사용자 생성
                
                생성된 사용자는 기본적으로 PENDING 상태로 등록되며,
                MASTER 또는 HUB_MANAGER의 승인 후 로그인 가능

                접근 권한:
                - 누구나
                """
    )
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
    @Operation(
            summary = "내 정보 수정",
            description = """
                로그인한 사용자의 Slack ID 수정

                접근 권한:
                - 모든 로그인 사용자
                - 본인 정보만 수정 가능
                """
    )
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
    @Operation(
            summary = "내 비밀번호 변경",
            description = """
                현재 비밀번호 확인 후 새 비밀번호로 변경

                접근 권한:
                - 모든 로그인 사용자
                - 본인 비밀번호만 변경 가능
                """
    )
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

    /**
     * 로그인한 사용자의 계정을 탈퇴 처리한다.
     */
    @Operation(
            summary = "회원 탈퇴",
            description = """
                로그인한 사용자의 계정을 Soft Delete

                접근 권한:
                - 모든 로그인 사용자
                - 본인 계정만 탈퇴 가능
                """
    )
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(
            Authentication authentication,
            @Valid @RequestBody UserWithdrawRequestDto request
    ) {
        /*
         * Filter가 Authentication principal에 넣은
         * 현재 로그인 사용자 정보를 꺼낸다.
         */
        AuthenticatedUser currentUser =
                (AuthenticatedUser) authentication.getPrincipal();

        WithdrawUserCommandDto command =
                new WithdrawUserCommandDto(
                        currentUser.userId(),
                        request.password()
                );

        userCommandService.withdraw(command);
    }


    /**
     * PENDING 상태의 가입 신청을 승인 또는 거절
     */
    @Operation(
            summary = "사용자 가입 승인 or 거절",
            description = """
                PENDING 상태의 사용자 가입 신청을 승인하거나 거절

                접근 권한:
                - MASTER
                - HUB_MANAGER: 담당 허브 소속 사용자만 처리 가능
                """
    )
    @PatchMapping("/{userId}/approval")
    public ApiResponse<UserApprovalResponseDto> changeApproval(
            @PathVariable Long userId,
            @Valid @RequestBody UserApprovalRequestDto request,
            Authentication authentication
    ) {
        AuthenticatedUser currentUser =
                (AuthenticatedUser) authentication.getPrincipal();

        ChangeApprovalResultDto result =
                userApprovalService.changeApproval(
                        request.toCommand(
                                userId,
                                currentUser.userId()
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

    /**
     * MASTER가 사용자를 관리자 권한으로 삭제
     */
    @Operation(
            summary = "사용자 관리자 삭제",
            description = """
                특정 사용자를 Soft Delete

                접근 권한:
                - MASTER
                - MASTER가 본인 계정 해당 API로 삭제 불가
                """
    )
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        /*
         * Gateway 검증 후 Filter가 SecurityContext에 등록한
         * 현재 요청자 정보
         */
        AuthenticatedUser currentUser =
                (AuthenticatedUser) authentication.getPrincipal();

        /*
         * 요청자와 삭제 대상 정보 전달
         */
        DeleteUserCommandDto command =
                new DeleteUserCommandDto(
                        currentUser.userId(),
                        currentUser.role(),
                        userId
                );

        userCommandService.deleteUser(command);
    }
}