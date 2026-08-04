package com.logistics.company.application.dto.result;

import java.util.UUID;

import com.logistics.company.application.dto.internal.HubInfoResponseDto;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyCreateResultDto(
		UUID companyId,
		String companyName,
		CompanyType companyType,
		String companyAddress,
		UUID hubId,
		String hubName
) {
	public static CompanyCreateResultDto from(HubInfoResponseDto hubInfo, Company company) {
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
