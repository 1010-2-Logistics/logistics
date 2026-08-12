package com.logistics.company.application.dto.internal.response;

import java.util.UUID;

import com.logistics.company.domain.entity.Role;

public record UserRoleUpdateResponseDto(
		Long userId,
		Role role,
		UUID companyId,
		UUID hubId
) {
}
