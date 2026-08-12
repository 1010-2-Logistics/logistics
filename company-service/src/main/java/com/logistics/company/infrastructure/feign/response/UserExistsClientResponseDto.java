package com.logistics.company.infrastructure.feign.response;

import java.util.UUID;

import com.logistics.company.domain.entity.Role;

public record UserExistsClientResponseDto(
		Long userId,
		Role role,
		UUID companyId,
		UUID hubId
) {

}
