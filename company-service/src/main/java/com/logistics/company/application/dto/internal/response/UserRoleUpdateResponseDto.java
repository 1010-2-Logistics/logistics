package com.logistics.company.application.dto.internal.response;

import java.util.UUID;

public record UserRoleUpdateResponseDto(
		UUID companyId,
		UUID hubId,
		Long userId,
		boolean exists
) {
}
