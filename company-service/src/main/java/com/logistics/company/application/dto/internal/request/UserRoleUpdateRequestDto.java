package com.logistics.company.application.dto.internal.request;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.Role;

public record UserRoleUpdateRequestDto(
		Long userId,
		UUID companyId,
		UUID hubId,
		Role role
) {
	public static UserRoleUpdateRequestDto from(Company company) {
		return null;
	}
}
