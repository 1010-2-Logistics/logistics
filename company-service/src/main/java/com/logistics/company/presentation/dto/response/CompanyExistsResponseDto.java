package com.logistics.company.presentation.dto.response;

import java.util.Optional;
import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyExistsResponseDto(
		UUID companyId,
		CompanyType companyType,
		UUID hubId,
		Long companyManagerId,
		boolean exists
) {
	public static CompanyExistsResponseDto from(Optional<Company> companyOptioanl) {
		if(companyOptioanl.isEmpty()) {
			return new CompanyExistsResponseDto(null, null, null, null, false);
		}
		
		Company company = companyOptioanl.get();
		
		return new CompanyExistsResponseDto(
				company.getCompanyId(),
				company.getCompanyType(),
				company.getHubId(),
				company.getCompanyManagerId(),
				true
		);
	}
}
