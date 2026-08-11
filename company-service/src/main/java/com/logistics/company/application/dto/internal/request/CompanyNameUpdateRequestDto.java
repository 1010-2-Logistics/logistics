package com.logistics.company.application.dto.internal.request;

import java.util.UUID;

public record CompanyNameUpdateRequestDto(
		UUID companyId,
		String companyName
) {
	public static CompanyNameUpdateRequestDto from(UUID companyId, String companyName) {
		return new CompanyNameUpdateRequestDto(companyId, companyName);
	}
}
