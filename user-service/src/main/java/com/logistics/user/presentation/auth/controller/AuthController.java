package com.logistics.user.presentation.auth.controller;

import com.logistics.user.application.dto.result.LoginResultDto;
import com.logistics.user.application.service.LoginService;
import com.logistics.user.application.service.SignupService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.presentation.auth.dto.request.LoginRequestDto;
import com.logistics.user.presentation.auth.dto.request.SignupRequestDto;
import com.logistics.user.presentation.auth.dto.response.LoginResponseDto;
import com.logistics.user.presentation.auth.dto.response.SignupResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 외부 API를 제공한다.
 *
 * 현재 구현 범위:
 * - 회원가입
 */
@Tag(
        name = "Auth API"
)
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupService signupService;
    private final LoginService loginService;

    /**
     * 회원가입 요청 처리
     * 인증받지 않은 사용자도 접근 가능
     */
    @Operation(
            summary = "회원가입",
            description = """
                새로운 사용자 계정 생성

                접근 권한:
                - 누구나
                - MASTER 권한으로는 회원가입 요청할 수 없음
                """
    )
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponseDto> signup(
            @Valid @RequestBody SignupRequestDto request
    ) {
        // HTTP 요청 DTO를 애플리케이션 Command로 변환한다.
        User user = signupService.signup(
                request.toCommand()
        );

        // Entity를 외부 응답 DTO로 변환한다.
        SignupResponseDto response =
                SignupResponseDto.from(user);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "회원가입 요청 성공",
                response
        );
    }

    /**
     * 사용자 로그인을 처리한다.
     *
     * 승인된 사용자에게 Access Token과 Refresh Token을 발급한다.
     */
    @Operation(
            summary = "로그인",
            description = """
                username과 password를 검증하고
                Access Token과 Refresh Token을 발급

                접근 권한:
                - 누구나
                """
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        // 1. HTTP DTO를 Application Command로 변환
        LoginResultDto result =
                loginService.login(
                        request.toCommand()
                );

        // 2. Application 결과를 HTTP Response DTO로 변환
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "로그인 성공",
                LoginResponseDto.from(result)
        );
    }
}