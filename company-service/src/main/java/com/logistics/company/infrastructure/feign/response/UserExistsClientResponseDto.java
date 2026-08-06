package com.logistics.company.infrastructure.feign.response;

public record UserExistsClientResponseDto(
		Long userId,
		boolean exists
) {

}
