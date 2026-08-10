package com.logistics.company.presentation.dto.response;

import java.util.UUID;

import com.logistics.company.application.dto.result.CompanyCreateResultDto;
import com.logistics.company.domain.entity.CompanyType;

public record CompanyCreateResponseDto(
		UUID companyId,
		String companyName,
		CompanyType companyType,
		String companyAddress,
		UUID hubId,
		String hubName
) {
	public static CompanyCreateResponseDto from(CompanyCreateResultDto result) {
		return new CompanyCreateResponseDto(
				result.companyId(),
				result.companyName(),
				result.companyType(),
				result.companyAddress(),
				result.hubId(),
				result.hubName()
		);
	}
}
