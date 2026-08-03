package com.logistics.company.application.dto.result;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;
import com.logistics.company.infrastructure.feign.response.HubValidationResponse;

public record CompanyCreateResultDto(
		UUID companyId,
		String companyName,
		CompanyType companyType,
		String companyAddress,
		UUID hubId,
		String hubName
) {
	public static CompanyCreateResultDto from(HubValidationResponse hubInfo, Company company) {
		return new CompanyCreateResultDto(
				company.getCompanyId(),
				company.getCompanyName(),
				company.getCompanyType(),
				company.getCompanyAddress(),
				hubInfo.hubId(),
				hubInfo.hubName()
		);
	}
}
