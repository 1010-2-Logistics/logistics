package com.logistics.company.infrastructure.feign.request;

import java.util.UUID;

import com.logistics.company.domain.entity.Role;

public record UserRoleUpdateClientRequestDto(
		Long userId,
		UUID companyId,
		UUID hubId,
		Role role
) {
}
