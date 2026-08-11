package com.logistics.slack.infrastructure.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "slack")
public record SlackProperties(
        // .env/application.yml 에서 Slack URL 가져와 보관하는 곳
        String baseUrl,
        String botToken
) {
}
