package com.logistics.slack.application.port;

public interface SlackMessageSender
{
    void send(
            String slackId,
            String message
    );
}
