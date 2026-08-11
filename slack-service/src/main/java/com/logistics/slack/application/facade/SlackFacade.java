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
    // userClient 조립
    private final SlackCommandService slackCommandService;
    private final SlackAuthorizationService slackAuthorizationService;
    private final UserPort userPort;

    public SlackCreateResult createSlack(
            SlackCreateCommand slackCreateCommand,
            AuthenticatedUser authenticatedUser
    ) {
        UserInfo receiver = userPort.getUser(slackCreateCommand.receiverId());

        slackAuthorizationService.validateAccess(
                authenticatedUser,
                receiver,
                slackCreateCommand.referenceId()
        );

        return slackCommandService.createSlack(
                slackCreateCommand,
                authenticatedUser
        );
    }

    public SlackRetryResult retrySlack(
            UUID slackMessageId,
            AuthenticatedUser authenticatedUser
    ) {
        return slackCommandService.retrySlack(
                slackMessageId,
                authenticatedUser
        );
    }
}
