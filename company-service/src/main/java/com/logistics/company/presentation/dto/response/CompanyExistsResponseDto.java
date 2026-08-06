package com.logistics.company.presentation.dto.response;

import java.util.Optional;
import java.util.UUID;

import com.logistics.company.domain.entity.Company;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyExistsResponseDto(
		UUID companyId,
		CompanyType companyType,
		boolean exists
) {
	public static CompanyExistsResponseDto from(Optional<Company> companyOptioanl) {
		if(companyOptioanl.isEmpty()) {
			return new CompanyExistsResponseDto(null, null, false);
		}
		
		Company company = companyOptioanl.get();
		
		return new CompanyExistsResponseDto(
				company.getCompanyId(),
				company.getCompanyType(),
				true
		);
	}
}
