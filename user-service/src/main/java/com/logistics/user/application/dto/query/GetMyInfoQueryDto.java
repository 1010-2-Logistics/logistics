package com.logistics.user.application.dto.query;

/**
 * 내 정보 조회에 필요한 입력값.
 */
public record GetMyInfoQueryDto(
        Long userId
) {
}