package com.logistics.user.presentation.controller;

import com.logistics.user.application.dto.query.GetMyInfoQueryDto;
import com.logistics.user.application.dto.query.GetUserDetailQueryDto;
import com.logistics.user.application.dto.query.GetUserQueryDto;
import com.logistics.user.application.dto.query.SearchUserQueryDto;
import com.logistics.user.application.dto.result.UserDetailResultDto;
import com.logistics.user.application.service.UserQueryService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.global.response.PageResponse;

import com.logistics.user.infrastructure.security.AuthenticatedUser;
import com.logistics.user.presentation.dto.response.UserResponseDto;
import com.logistics.user.presentation.dto.response.UserSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class QueryController {

    private final UserQueryService UserQueryService;

    /**
     * 로그인한 사용자의 정보를 조회한다.
     */
    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo(
            Authentication authentication
    ) {
        /*
         * Filter가 Authentication principal에 넣은
         * 현재 사용자 인증 정보를 꺼낸다.
         */
        AuthenticatedUser currentUser =
                (AuthenticatedUser) authentication.getPrincipal();

        GetMyInfoQueryDto query =
                new GetMyInfoQueryDto(
                        currentUser.userId()
                );

        UserDetailResultDto result =
                UserQueryService.getMyInfo(query);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "내 정보 조회 성공",
                UserResponseDto.from(result)
        );
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponseDto> getUserDetail(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        AuthenticatedUser currentUser =
                (AuthenticatedUser) authentication.getPrincipal();

        GetUserDetailQueryDto query =
                new GetUserDetailQueryDto(
                        currentUser.userId(),
                        currentUser.role(),
                        currentUser.hubId(),
                        userId
                );

        UserDetailResultDto result =
                UserQueryService.getUserDetail(query);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "사용자 상세 조회 성공",
                UserResponseDto.from(result)
        );
    }

    @GetMapping
    public ApiResponse<PageResponse<UserSummaryResponseDto>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<User> page = UserQueryService.search(new SearchUserQueryDto(keyword, pageable));
        Page<UserSummaryResponseDto> responsePage = page.map(UserSummaryResponseDto::from);
        return ApiResponse.success(200, "샘플 목록 조회 성공", PageResponse.of(responsePage));
    }
}