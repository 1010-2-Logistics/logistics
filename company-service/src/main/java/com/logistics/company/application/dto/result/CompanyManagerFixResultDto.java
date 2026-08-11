package com.logistics.company.application.dto.result;

import java.util.UUID;

public record CompanyManagerFixResultDto(
		boolean success,
		UUID companyId,
		Long companyManagerId
) {

}
