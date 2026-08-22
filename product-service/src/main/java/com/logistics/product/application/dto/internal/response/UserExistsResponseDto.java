package com.logistics.product.application.dto.internal.response;

import java.util.UUID;

import com.logistics.product.domain.entity.Role;

public record UserExistsResponseDto(
		Long userId,
		Role role,
		UUID companyId,
		UUID hubId
) {

}
