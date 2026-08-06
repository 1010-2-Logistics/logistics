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
	public static UserRoleUpdateRequestDto from(Long companyManagerId, Company company) {
		return new UserRoleUpdateRequestDto(
				companyManagerId,
				company.getCompanyId(),
				company.getHubId(),
				Role.COMPANY_MANAGER
		);
	}
}
