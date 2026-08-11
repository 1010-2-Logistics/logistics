package com.logistics.slack.infrastructure.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class SlackApiConfig {
    @Bean
    public RestClient slackRestClient(
            SlackProperties slackProperties
    ) {
        return RestClient.builder()
                .baseUrl(slackProperties.baseUrl())
                .defaultHeader(
                        "Authorization",
                        "Bearer " + slackProperties.botToken()
                )
                .defaultHeader(
                        "Content-Type",
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }
}
