package com.logistics.user.presentation.dto.response;

import com.logistics.user.application.dto.result.UpdateMyInfoResultDto;
import java.time.LocalDateTime;

/**
 * 내 정보 수정 API의 HTTP 응답 객체.
 */
public record UserUpdateResponseDto(
        Long userId,
        String username,
        String slackId,
        LocalDateTime updatedAt
) {

    public static UserUpdateResponseDto from(
            UpdateMyInfoResultDto result
    ) {
        return new UserUpdateResponseDto(
                result.userId(),
                result.username(),
                result.slackId(),
                result.updatedAt()
        );
    }
}