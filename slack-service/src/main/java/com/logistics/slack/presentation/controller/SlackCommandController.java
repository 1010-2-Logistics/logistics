package com.logistics.slack.presentation.controller;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.application.dto.result.SlackRetryResult;
import com.logistics.slack.application.facade.SlackFacade;
import com.logistics.slack.application.service.SlackCommandService;
import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.infrastructure.security.principal.UserPrincipal;
import com.logistics.slack.presentation.dto.request.SlackCreateRequestDto;
import com.logistics.slack.presentation.dto.response.SlackCreateResponseDto;
import com.logistics.slack.presentation.dto.response.SlackRetryResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/slack/messages")
@RequiredArgsConstructor
public class SlackCommandController {
    private final SlackFacade slackFacade;
    private final SlackCommandService slackCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SlackCreateResponseDto> createSlack(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SlackCreateRequestDto slackCreateRequestDto
    ) {
        SlackCreateCommand slackCreateCommand = SlackCreateCommand.toCommand(
                principal.getUserId(),
                slackCreateRequestDto
        );

        SlackCreateResult slackCreatResult = slackFacade.createSlack(
                slackCreateCommand,
                principal.toAuthenticatedUser()
        );

        SlackCreateResponseDto slackCreateResponseDto = SlackCreateResponseDto.from(slackCreatResult);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Slack 메시지 발송 요청 성공",
                slackCreateResponseDto
        );
    }

    @PostMapping("/{slackMessageId}/retry")
    public ApiResponse<SlackRetryResponseDto> retrySlack(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("slackMessageId") UUID slackMessageId
    ) {
        SlackRetryResult slackRetryResult = slackFacade.retrySlack(
                slackMessageId,
                principal.toAuthenticatedUser()
        );

        SlackRetryResponseDto slackRetryResponseDto = SlackRetryResponseDto.from(slackRetryResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Slack 메시지 재발송 요청 성공",
                slackRetryResponseDto
        );
    }

    @DeleteMapping("/{slackMessageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("slackMessageId") UUID slackMessageId
    ) {
        slackCommandService.deleteSlack(
                slackMessageId,
                principal.toAuthenticatedUser()
        );
    }
}
