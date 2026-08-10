package com.logistics.user.application.dto.result;

import com.logistics.user.domain.entity.User;

public record InternalUserResultDto(
        Long userId,
        String name,
        String slackId
) {

    public static InternalUserResultDto from(
            User user
    ) {
        return new InternalUserResultDto(
                user.getUserId(),
                user.getName(),
                user.getSlackId()
        );
    }
}