package com.logistics.slack.infrastructure.slack;


import com.logistics.slack.application.port.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SlackApiMessageSender implements SlackMessageSender {
    // RestClient란? 자바 코드에서 다른 서버로 HTTP 요청을 보내는 도구
    private final RestClient slackRestClient;

    @Override
    public void send(
            String receiverSlackId,
            String message
    ) {
        SlackApiRequest request = new SlackApiRequest(
                receiverSlackId,
                message
        );

        SlackApiResponse response = slackRestClient.post()
                .uri("/chat.postMessage")
                .body(request)
                .retrieve()
                .body(SlackApiResponse.class);

        if (response == null || !response.ok()) {
            throw new RuntimeException(
                    response != null ? response.error() : "Slack API 응답이 없습니다."
            );
        }
    }
}
