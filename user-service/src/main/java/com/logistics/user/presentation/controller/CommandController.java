package com.logistics.user.presentation.controller;

import com.logistics.user.application.dto.command.UpdateUserCommand;
import com.logistics.user.application.facade.UserFacade;
import com.logistics.user.application.service.UserCommandService;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.presentation.controller.dto.request.UserCreateRequest;
import com.logistics.user.presentation.controller.dto.request.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class CommandController {

    private final UserFacade userFacade;
    private final UserCommandService userCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(
            @Valid @RequestBody UserCreateRequest request
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
            @Valid @RequestBody UserUpdateRequest request
    ) {
        userCommandService.update(
                new UpdateUserCommand(
                        userId,
                        request.name()
                )
        );

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
}