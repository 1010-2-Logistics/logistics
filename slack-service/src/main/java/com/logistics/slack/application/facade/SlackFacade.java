package com.logistics.slack.application.facade;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.application.dto.result.SlackRetryResult;
import com.logistics.slack.application.service.SlackCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SlackFacade {
    // userClient 조립
    private final SlackCommandService slackCommandService;

    public SlackCreateResult createSlack(
            SlackCreateCommand slackCreateCommand
    ) {
        return slackCommandService.createSlack(slackCreateCommand);
    }

    public SlackRetryResult retrySlack(
            UUID slackMessageId
    ) {
        return slackCommandService.retrySlack(slackMessageId);
    }
}
