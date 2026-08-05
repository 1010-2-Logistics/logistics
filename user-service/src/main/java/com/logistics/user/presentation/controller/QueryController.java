package com.logistics.user.presentation.controller;

import com.logistics.user.application.dto.query.GetUserQuery;
import com.logistics.user.application.dto.query.SearchUserQuery;
import com.logistics.user.application.service.UserQueryService;
import com.logistics.user.domain.entity.User;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.global.response.PageResponse;

import com.logistics.user.presentation.dto.response.UserResponseDto;
import com.logistics.user.presentation.dto.response.UserSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/Users")
@RequiredArgsConstructor
public class QueryController {

    private final UserQueryService UserQueryService;

    @GetMapping("/{UserId}")
    public ApiResponse<UserResponseDto> get(@PathVariable Long UserId) {
        User User = UserQueryService.get(new GetUserQuery(UserId));
        return ApiResponse.success(200, "샘플 조회 성공", UserResponseDto.from(User));
    }

    @GetMapping
    public ApiResponse<PageResponse<UserSummaryResponseDto>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<User> page = UserQueryService.search(new SearchUserQuery(keyword, pageable));
        Page<UserSummaryResponseDto> responsePage = page.map(UserSummaryResponseDto::from);
        return ApiResponse.success(200, "샘플 목록 조회 성공", PageResponse.of(responsePage));
    }
}