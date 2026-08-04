package com.logistics.user.presentation.auth.controller;

import com.logistics.user.application.service.SignupService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.presentation.auth.dto.request.SignupRequestDto;
import com.logistics.user.presentation.auth.dto.response.SignupResponseDto;
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
 *
 * 추후 로그인, 토큰 재발급, 로그아웃이 추가된다.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupService signupService;

    /**
     * 회원가입 요청을 처리한다.
     *
     * 인증되지 않은 사용자도 접근할 수 있다.
     */
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
}