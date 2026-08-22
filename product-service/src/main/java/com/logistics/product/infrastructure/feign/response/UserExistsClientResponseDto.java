package com.logistics.product.infrastructure.feign.response;

import java.util.UUID;

import com.logistics.product.domain.entity.Role;

public record UserExistsClientResponseDto(
		Long userId,
		Role role,
		UUID companyId,
		UUID hubId
) {

}
