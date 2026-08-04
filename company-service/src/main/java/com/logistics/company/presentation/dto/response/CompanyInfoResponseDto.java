package com.logistics.company.presentation.dto.response;

import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyInfoResponseDto(
		UUID companyId,
		UUID hubId,
		String companyName,
		CompanyType companyType,
		String companyAddress
) {
	public static CompanyInfoResponseDto from(Company company) {
		return new CompanyInfoResponseDto(
				company.getCompanyId(),
				company.getHubId(),
				company.getCompanyName(),
				company.getCompanyType(),
				company.getCompanyAddress()
		);
	}
}
