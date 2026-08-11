package com.logistics.company.infrastructure.feign.response;

import java.util.UUID;

public record CompanyNameUpdateClientResponseDto(
		int productCount,
		int updateFailCount,
		UUID companyId,
		String companyName,
		boolean exists
) {
	
}
