package com.logistics.slack.presentation.controller;

import com.logistics.slack.application.dto.query.SlackSearchQuery;
import com.logistics.slack.application.dto.result.SlackDetailResult;
import com.logistics.slack.application.dto.result.SlackListResult;
import com.logistics.slack.application.service.SlackQueryService;
import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.global.response.PageResponse;
import com.logistics.slack.infrastructure.security.principal.UserPrincipal;
import com.logistics.slack.presentation.dto.request.SlackSearchRequestDto;
import com.logistics.slack.presentation.dto.response.SlackDetailResponseDto;
import com.logistics.slack.presentation.dto.response.SlackListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Slack")
@RestController
@RequestMapping("/api/v1/slack/messages")
@RequiredArgsConstructor
public class SlackQueryController {

    private final SlackQueryService slackQueryService;

    @Operation(
            summary = "Slack 메시지 발송 이력 상세 조회",
            description = """
                    접근 권한:
                    - MASTER : 모든 Slack 메시지 발송 이력 상세 조회 가능
                    """
    )
    @GetMapping("/{slackMessageId}")
    public ApiResponse<SlackDetailResponseDto> getSlack(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("slackMessageId") UUID slackMessageId
    ) {
        SlackDetailResult slackDetailResult = slackQueryService.getSlack(
                slackMessageId,
                principal.toAuthenticatedUser()
        );

        SlackDetailResponseDto slackDetailResponseDto = SlackDetailResponseDto.from(slackDetailResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Slack 메시지 발송 이력 상세 조회 성공",
                slackDetailResponseDto
        );
    }

    @Operation(
            summary = "Slack 메시지 발송 이력 목록 조회",
            description = """
                    접근 권한:
                    - MASTER : 모든 Slack 메시지 발송 이력 목록 조회 가능
                    """
    )
    @GetMapping
    public ApiResponse<PageResponse<SlackListResponseDto>> getSlacks(
            @AuthenticationPrincipal UserPrincipal principal,
            @ModelAttribute SlackSearchRequestDto slackSearchRequestDto
    ) {
        SlackSearchQuery slackSearchQuery = SlackSearchQuery.from(
                slackSearchRequestDto
        );

        Page<SlackListResult> slackListResultPage = slackQueryService.getSlacks(
                slackSearchQuery,
                principal.toAuthenticatedUser()
        );

        Page<SlackListResponseDto> slackListResponseDtoPage = slackListResultPage.map(SlackListResponseDto::from);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Slack 메시지 발송 이력 목록 조회 성공",
                PageResponse.of(slackListResponseDtoPage)
        );
    }
}
