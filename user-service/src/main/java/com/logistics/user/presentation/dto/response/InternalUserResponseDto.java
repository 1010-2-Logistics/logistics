package com.logistics.user.presentation.dto.response;

import com.logistics.user.application.dto.result.InternalUserResultDto;

public record InternalUserResponseDto(
        Long userId,
        String name,
        String slackId
) {

    public static InternalUserResponseDto from(
            InternalUserResultDto result
    ) {
        return new InternalUserResponseDto(
                result.userId(),
                result.name(),
                result.slackId()
        );
    }
}