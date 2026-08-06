package com.logistics.company.application.dto.command;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyCreateCommand(
		UUID hubId,
		Long companyManagerId,
		String companyName,
		String companyAddress,
		CompanyType companyType
) {
	public Company toEntity() {
		return Company.create(hubId, companyManagerId, companyName, companyAddress, companyType);
	}
}
