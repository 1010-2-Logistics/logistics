package com.logistics.user.application.dto.command;

import com.logistics.user.domain.entity.UserRole;

/**
 * MASTER가 사용자를 삭제하기 위한 입력값.
 */
public record DeleteUserCommandDto(
        Long requesterId,
        UserRole requesterRole,
        Long targetUserId
) {
}