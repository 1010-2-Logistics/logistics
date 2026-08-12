package com.logistics.slack.application.facade;

import com.logistics.slack.application.authorization.SlackAuthorizationService;
import com.logistics.slack.application.dto.auth.AuthenticatedUser;
import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.application.dto.result.SlackRetryResult;
import com.logistics.slack.application.dto.result.UserInfo;
import com.logistics.slack.application.port.UserPort;
import com.logistics.slack.application.service.SlackCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SlackFacade {
    private final SlackCommandService slackCommandService;
    private final UserPort userPort;
    private final SlackAuthorizationService slackAuthorizationService;

    public SlackCreateResult createSlack(
            SlackCreateCommand slackCreateCommand,
            AuthenticatedUser authenticatedUser
    ) {
        UserInfo receiver = userPort.getUser(
                slackCreateCommand.receiverId()
        );

        slackAuthorizationService.validateCreateAccess(
                authenticatedUser
        );

        return slackCommandService.createSlack(
                slackCreateCommand,
                receiver.slackId()
        );
    }

    public SlackRetryResult retrySlack(
            UUID slackMessageId,
            AuthenticatedUser authenticatedUser
    ) {
        slackAuthorizationService.validateRetryAccess(
                authenticatedUser
        );

        Long receiverId = slackCommandService.getReceiverId(
                slackMessageId
        );

        UserInfo receiver = userPort.getUser(
                receiverId
        );

        return slackCommandService.retrySlack(
                slackMessageId,
                receiver.slackId()
        );
    }
}