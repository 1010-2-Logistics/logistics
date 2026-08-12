package com.logistics.slack.application.port;

public interface SlackMessageSender
{
    void send(
            String receiverSlackId,
            String message
    );
}
