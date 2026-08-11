package com.logistics.slack.infrastructure.feign.response;

import java.util.UUID;

public record UserInfoResponse (
        Long userId,
        String slackId,
        String role,
        UUID hubId,
        UUID companyId
){
}
