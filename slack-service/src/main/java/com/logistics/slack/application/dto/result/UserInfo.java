package com.logistics.slack.application.dto.result;

import com.logistics.slack.domain.entity.Role;

import java.util.UUID;

public record UserInfo(
        Long userId,
        String slackId,
        Role role,
        UUID hubId,
        UUID companyId
) {
}
