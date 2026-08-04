package com.logistics.company.application.dto.command;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyCreateCommand(
		UUID hubId,
		String companyName,
		String companyAddress,
		CompanyType companyType
) {
	public Company toEntity() {
		return Company.create(hubId, companyName, companyAddress, companyType);
	}
}
