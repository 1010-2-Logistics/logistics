package com.logistics.slack.application.facade;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.presentation.dto.response.SlackRetryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SlackFacade {
    // userClient 조립
    public SlackCreateResult createSlack(
            SlackCreateCommand slackCreateCommand
    ) {
        return null;
    }

    public SlackRetryResponseDto retrySlackMessage(UUID slackMessageId) {
        return null;
    }

}
