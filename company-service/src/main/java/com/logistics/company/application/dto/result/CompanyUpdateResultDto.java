package com.logistics.company.application.dto.result;

import java.util.UUID;

import com.logistics.company.application.dto.internal.response.CompanyNameUpdateResponseDto;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyUpdateResultDto(
		int updateCount,
		int updateFailCount,
		UUID companyId,
		String companyName,
		CompanyType companyType,
		String companyAddress
) {
	public static CompanyUpdateResultDto from(Company company, CompanyNameUpdateResponseDto companyNameUpdate) {
		return new CompanyUpdateResultDto(
				companyNameUpdate.updateCount(),
				companyNameUpdate.updateFailCount(),
				company.getCompanyId(),
				company.getCompanyName(),
				company.getCompanyType(),
				company.getCompanyAddress()
		);
	}
}
