package com.logistics.company.infrastructure.feign.response;

import java.util.UUID;

public record UserRoleUpdateClientResponseDto(
		UUID companyId,
		UUID hubId,
		Long userId,
		boolean exists
) {
	
}
