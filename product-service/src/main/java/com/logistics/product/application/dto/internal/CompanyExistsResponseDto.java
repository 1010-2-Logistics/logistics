package com.logistics.product.application.dto.internal;

import java.util.UUID;

import com.logistics.product.domain.entity.CompanyType;

public record CompanyExistsResponseDto(
		UUID companyId,
		CompanyType companyType,
		UUID hubId,
		Long companyManagerId,
		boolean exists
) {

}
