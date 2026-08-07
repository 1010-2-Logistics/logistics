package com.logistics.user.application.dto.result;

import com.logistics.user.domain.entity.User;
import java.time.LocalDateTime;

/**
 * 비밀번호 변경 결과.
 */
public record ChangePasswordResultDto(
        Long userId
) {

    public static ChangePasswordResultDto from(User user) {
        return new ChangePasswordResultDto(
                user.getUserId()
        );
    }
}