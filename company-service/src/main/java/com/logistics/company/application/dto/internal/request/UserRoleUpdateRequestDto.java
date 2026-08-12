package com.logistics.company.application.dto.internal.request;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.Role;

public record UserRoleUpdateRequestDto(
		UUID companyId,
		UUID hubId,
		Role role
) {
	public static UserRoleUpdateRequestDto from(Company company) {
		return new UserRoleUpdateRequestDto(
				company.getCompanyId(),
				company.getHubId(),
				Role.COMPANY_MANAGER
		);
	}
}
