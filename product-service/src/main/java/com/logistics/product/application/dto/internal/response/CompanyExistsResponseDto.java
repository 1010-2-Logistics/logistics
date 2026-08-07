package com.logistics.product.application.dto.internal.response;

import java.util.UUID;

import com.logistics.product.domain.entity.CompanyType;

public record CompanyExistsResponseDto(
		UUID companyId,
		CompanyType companyType,
		String companyName,
		UUID hubId,
		Long companyManagerId,
		boolean exists
) {

}
