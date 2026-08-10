package com.logistics.user.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 최초 MASTER 계정 생성에 필요한 설정값.
 *
 * 실제 값은 application.yml에 직접 저장하지 않고
 * 환경변수에서 전달받는다.
 */
@ConfigurationProperties(
        prefix = "app.master"
)
public record MasterAccountProperties(
        String username,
        String name,
        String password,
        String slackId
) {
}