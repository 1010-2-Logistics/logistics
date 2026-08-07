package com.logistics.slack.presentation.controller;


import com.logistics.slack.application.port.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/slack")
@RequiredArgsConstructor
public class SlackTestController {
    private final SlackMessageSender slackMessageSender;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendTestMessage() {
        slackMessageSender.send("Slack WebHook 연동 테스트 메시지");
    }
}
