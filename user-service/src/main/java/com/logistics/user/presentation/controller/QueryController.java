package com.logistics.user.presentation.controller;

import com.logistics.user.application.dto.query.GetMyInfoQueryDto;
import com.logistics.user.application.dto.query.GetUserDetailQueryDto;
import com.logistics.user.application.dto.query.GetUserQueryDto;
import com.logistics.user.application.dto.query.SearchUserQueryDto;
import com.logistics.user.application.dto.result.UserDetailResultDto;
import com.logistics.user.application.service.UserQueryService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.global.response.PageResponse;

import com.logistics.user.infrastructure.security.AuthenticatedUser;
import com.logistics.user.presentation.dto.response.UserResponseDto;
import com.logistics.user.presentation.dto.response.UserSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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

    /**
     * 사용자 정보 상세 조회 (MASTER, 허브 매니저는 본인 허브 소속 사용자만 조회 가능)
     */
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

    /**
     * 사용자 정보 목록 조회
     */
    @GetMapping
    public ApiResponse<PageResponse<UserSummaryResponseDto>> search(
            Authentication authentication,

            @RequestParam(required = false)
            String username,

            @RequestParam(required = false)
            UserStatus status,

            @RequestParam(required = false)
            UserRole role,

            @RequestParam(required = false)
            UUID hubId,

            @RequestParam(required = false)
            UUID companyId,

            @RequestParam(defaultValue = "createdAt")
            String sort,

            @RequestParam(defaultValue = "DESC")
            String direction,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        AuthenticatedUser currentUser =
                (AuthenticatedUser) authentication.getPrincipal();

        int validatedSize =
                (size == 10 || size == 30 || size == 50)
                        ? size
                        : 10;

        Sort.Direction sortDirection =
                "ASC".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        validatedSize,
                        Sort.by(
                                sortDirection,
                                resolveSortField(sort)
                        )
                );

        SearchUserQueryDto query =
                new SearchUserQueryDto(
                        currentUser.role(),
                        currentUser.hubId(),
                        username,
                        status,
                        role,
                        hubId,
                        companyId,
                        pageable
                );

        Page<User> userPage =
                UserQueryService.search(query);

        Page<UserSummaryResponseDto> responsePage =
                userPage.map(
                        UserSummaryResponseDto::from
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "사용자 목록 조회 성공",
                PageResponse.of(responsePage)
        );
    }

    private String resolveSortField(
            String sort
    ) {
        return switch (sort) {

            case "username" -> "username";

            case "status" -> "status";

            case "createdAt" -> "createdAt";

            default -> "createdAt";
        };
    }
}