package com.logistics.product.infrastructure.feign.response;

import java.util.UUID;

import com.logistics.product.domain.entity.CompanyType;

public record CompanyExistsClientResponseDto(
		UUID companyId,
		CompanyType companyType,
		String companyName,
		UUID hubId,
		Long companyManagerId,
		boolean exists
) {

}
