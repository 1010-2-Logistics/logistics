package com.logistics.company.presentation.dto.response;

import java.util.UUID;

import com.logistics.company.application.dto.result.CompanyUpdateResultDto;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyUpdateResponseDto(
		UUID companyId,
		String companyName,
		CompanyType companyType,
		String companyAddress
) {

	public static CompanyUpdateResponseDto from(CompanyUpdateResultDto result) {
		return new CompanyUpdateResponseDto(
				result.companyId(),
				result.companyName(),
				result.companyType(),
				result.companyAddress()
		);
	}

}
