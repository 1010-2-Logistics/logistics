package com.logistics.slack.application.port;

import com.logistics.slack.application.event.SlackSendEvent;

public interface SlackEventPublisher {
    void publish(SlackSendEvent slackSendEvent);
}
