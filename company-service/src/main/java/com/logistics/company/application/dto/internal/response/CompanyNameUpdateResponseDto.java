package com.logistics.company.application.dto.internal.response;

import java.util.UUID;

public record CompanyNameUpdateResponseDto(
		int updateCount,
		int updateFailCount,
		UUID companyId,
		boolean exists
) {

}
