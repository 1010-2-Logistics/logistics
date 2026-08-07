package com.logistics.user.application.dto.result;

import com.logistics.user.domain.entity.User;
import java.time.LocalDateTime;

/**
 * 내 정보 수정 유스케이스의 처리 결과.
 */
public record UpdateMyInfoResultDto(
        Long userId,
        String username,
        String slackId,
        LocalDateTime updatedAt
) {

    public static UpdateMyInfoResultDto from(User user) {
        return new UpdateMyInfoResultDto(
                user.getUserId(),
                user.getUsername(),
                user.getSlackId(),
                user.getUpdatedAt()
        );
    }
}