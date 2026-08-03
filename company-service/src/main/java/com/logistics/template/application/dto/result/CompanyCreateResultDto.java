package com.logistics.template.application.dto.result;

import java.util.UUID;

import com.logistics.template.domain.entity.Company;
import com.logistics.template.domain.entity.CompanyType;
import com.logistics.template.infrastructure.feign.response.HubValidationResponse;

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
