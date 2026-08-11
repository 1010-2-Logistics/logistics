package com.logistics.company.application.dto.command;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.domain.entity.Role;

public record CompanyCreateCommand(
		Long userId,
		Role role,
		UUID userHubId,
		UUID hubId,
		Long companyManagerId,
		String companyName,
		String companyAddress,
		CompanyType companyType
) {
	public Company toEntity() {
		return Company.create(hubId, companyName, companyAddress, companyType);
	}
}
