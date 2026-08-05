package com.logistics.slack.presentation.controller;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.command.UpdateSampleCommand;
import com.logistics.slack.application.dto.result.SlackCreatResult;
import com.logistics.slack.application.facade.SlackFacade;
import com.logistics.slack.application.service.SampleCommandService;
import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.presentation.dto.request.SlackCreateRequestDto;
import com.logistics.slack.presentation.dto.response.SlackCreateResponseDto;
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
public class CommandController {
    private final SlackFacade slackFacade;
    private final SampleCommandService sampleCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SlackCreateResponseDto> create(
            @Valid @RequestBody SlackCreateRequestDto slackCreateRequestDto
    ) {
        SlackCreateCommand slackCreateCommand = SlackCreateCommand.toCommand(slackCreateRequestDto);

        SlackCreatResult slackCreatResult = slackFacade.createSlack(slackCreateCommand);

        SlackCreateResponseDto slackCreateResponseDto = SlackCreateResponseDto.from(slackCreatResult);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Slack 메시지 발송 요청 성공",
                slackCreateResponseDto
        );
    }

    @PutMapping("/{sampleId}")
    public ApiResponse<Void> update(
            @PathVariable UUID sampleId,
            @Valid @RequestBody SampleUpdateRequest request
    ) {
        sampleCommandService.updateSlack(new UpdateSampleCommand(sampleId, request.name()));
        return ApiResponse.success(
                200,
                "샘플 수정 성공",
                null
        );
    }

    @DeleteMapping("/{sampleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID sampleId
    ) {
        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
        sampleCommandService.deleteSlack(sampleId, "system");
    }
}
