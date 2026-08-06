package com.logistics.slack.presentation.controller;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.application.facade.SlackFacade;
import com.logistics.slack.application.service.SlackCommandService;
import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.presentation.dto.request.SlackCreateRequestDto;
import com.logistics.slack.presentation.dto.response.SlackCreateResponseDto;
import com.logistics.slack.presentation.dto.response.SlackRetryResponseDto;
import jakarta.validation.Valid;

import java.util.UUID;

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
@RequestMapping("/api/v1/slack/messages")
@RequiredArgsConstructor
public class SlackCommandController {
    private final SlackFacade slackFacade;
    private final SlackCommandService slackCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SlackCreateResponseDto> createSlack(
            @Valid @RequestBody SlackCreateRequestDto slackCreateRequestDto
    ) {
        // TODO : 인증 구현 후 로그인 사용자 교체
        Long senderId = 1L;

        SlackCreateCommand slackCreateCommand = SlackCreateCommand.toCommand(
                senderId,
                slackCreateRequestDto
        );

        SlackCreateResult slackCreatResult = slackFacade.createSlack(
                slackCreateCommand
        );

        SlackCreateResponseDto slackCreateResponseDto = SlackCreateResponseDto.from(slackCreatResult);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Slack 메시지 발송 요청 성공",
                slackCreateResponseDto
        );
    }

    @PutMapping("/{slackMessageId}/retry")
    public ApiResponse<SlackRetryResponseDto> updateSlack(
            @PathVariable("slackMessageId") UUID slackMessageId
    ) {
//        SlackRetryResponseDto slackRetryResponseDto = slackFacade.retrySlackMessage(slackMessageId);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Slack 메시지 재발송 요청 성공",
//                slackRetryResponseDto
                null
        );
    }

    @DeleteMapping("/{slackMessageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("slackMessageId") UUID slackMessageId
    ) {
        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
        slackCommandService.deleteSlack(slackMessageId, "system");
    }
}
