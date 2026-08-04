package com.logistics.company.application.dto.result;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyUpdateResultDto(
		UUID companyId,
		String companyName,
		CompanyType companyType,
		String companyAddress
) {
	public static CompanyUpdateResultDto from(Company company) {
		return new CompanyUpdateResultDto(
				company.getCompanyId(),
				company.getCompanyName(),
				company.getCompanyType(),
				company.getCompanyAddress()
		);
	}
}
