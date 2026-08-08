package com.logistics.slack.presentation.controller;

import com.logistics.slack.application.dto.query.SlackSearchQuery;
import com.logistics.slack.application.dto.result.SlackDetailResult;
import com.logistics.slack.application.dto.result.SlackListResult;
import com.logistics.slack.application.service.SlackQueryService;
import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.global.response.PageResponse;
import com.logistics.slack.presentation.dto.request.SlackSearchRequestDto;
import com.logistics.slack.presentation.dto.response.SlackDetailResponseDto;
import com.logistics.slack.presentation.dto.response.SlackListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/slack/messages")
@RequiredArgsConstructor
public class SlackQueryController {

    private final SlackQueryService slackQueryService;

    @GetMapping("/{slackMessageId}")
    public ApiResponse<SlackDetailResponseDto> getSlack(
            @PathVariable("slackMessageId") UUID slackMessageId
    ) {
        SlackDetailResult slackDetailResult = slackQueryService.getSlack(slackMessageId);

        SlackDetailResponseDto slackDetailResponseDto = SlackDetailResponseDto.from(slackDetailResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Slack 메시지 발송 이력 상세 조회 성공",
                slackDetailResponseDto
        );
    }

    @GetMapping
    public ApiResponse<PageResponse<SlackListResponseDto>> getSlacks(
            @ModelAttribute SlackSearchRequestDto slackSearchRequestDto
    ) {
        SlackSearchQuery slackSearchQuery = SlackSearchQuery.from(
                slackSearchRequestDto
        );

        Page<SlackListResult> slackListResultPage = slackQueryService.getSlacks(slackSearchQuery);

        Page<SlackListResponseDto> slackListResponseDtoPage = slackListResultPage.map(SlackListResponseDto::from);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Slack 메시지 발송 이력 목록 조회 성공",
                PageResponse.of(slackListResponseDtoPage)
        );
    }
}
