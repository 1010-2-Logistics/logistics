package com.logistics.slack.infrastructure.slack;


import com.logistics.slack.application.port.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SlackWebhookSender implements SlackMessageSender {
    private final RestClient slackRestClient;

    @Override
    public void send(String message) {
        slackRestClient.post()
                .body(new SlackWebhookRequest(message))
                .retrieve()
                .toBodilessEntity();
    }
}
