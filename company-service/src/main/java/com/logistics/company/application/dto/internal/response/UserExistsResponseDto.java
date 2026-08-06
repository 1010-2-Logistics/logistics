package com.logistics.company.application.dto.internal.response;

public record UserExistsResponseDto(
		Long userId,
		boolean exists
) {
}
