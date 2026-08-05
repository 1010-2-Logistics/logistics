package com.logistics.slack.infrastructure.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SlackWebhookConfig {
    @Bean
    public RestClient slackRestClient(
            SlackProperties slackProperties
    ) {
        return RestClient.builder()
                .baseUrl(slackProperties.webhookUrl())
                .build();
    }
}
