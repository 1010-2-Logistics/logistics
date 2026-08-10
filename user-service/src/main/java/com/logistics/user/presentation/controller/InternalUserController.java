package com.logistics.user.presentation.controller;

import com.logistics.user.application.dto.command.ChangeUserAffiliationCommandDto;
import com.logistics.user.application.dto.result.ChangeUserAffiliationResultDto;
import com.logistics.user.application.service.UserAffiliationService;
import com.logistics.user.global.response.ApiResponse;
import com.logistics.user.presentation.dto.request.ChangeUserAffiliationRequestDto;
import com.logistics.user.presentation.dto.response.ChangeUserAffiliationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * user-service 내부 API 호출 컨트롤러
 */
@RestController
@RequestMapping("/internal/v1/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserAffiliationService userAffiliationService;

    /**
     * 사용자의 소속 및 권한 변경
     */
    @PatchMapping("/{userId}/affiliation")
    public ApiResponse<ChangeUserAffiliationResponseDto> changeAffiliation(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeUserAffiliationRequestDto request
    ) {
        // HTTP 요청을 Application Command로 변환
        ChangeUserAffiliationCommandDto command =
                request.toCommand(userId);

        ChangeUserAffiliationResultDto result =
                userAffiliationService.changeAffiliation(command);

        // 3. Application Result를 HTTP Response로 변환
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "소속 및 권한 변경 완료",
                ChangeUserAffiliationResponseDto.from(result)
        );
    }
}