package com.logistics.slack.presentation.controller;

import com.logistics.slack.application.dto.query.SlackSearchQuery;
import com.logistics.slack.application.dto.result.SlackDetailResult;
import com.logistics.slack.application.dto.result.SlackListResult;
import com.logistics.slack.application.service.SlackQueryService;
import com.logistics.slack.domain.entity.SlackStatus;
import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.global.response.PageResponse;
import com.logistics.slack.presentation.dto.response.SlackDetailResponseDto;
import com.logistics.slack.presentation.dto.response.SlackListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
            @RequestParam(required = false) SlackStatus status,
            @RequestParam(required = false) Long senderId,
            @RequestParam(required = false) Long receiverId,
            @RequestParam(required = false) UUID referenceId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdTo,

            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        SlackSearchQuery slackSearchQuery = SlackSearchQuery.of(
                status,
                senderId,
                receiverId,
                referenceId,
                createdFrom,
                createdTo,
                pageable
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
