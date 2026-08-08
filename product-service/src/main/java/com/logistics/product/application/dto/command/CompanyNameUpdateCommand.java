package com.logistics.product.application.dto.command;

import java.util.UUID;

public record CompanyNameUpdateCommand(
		UUID companyId,
		String companyName
) {

}
