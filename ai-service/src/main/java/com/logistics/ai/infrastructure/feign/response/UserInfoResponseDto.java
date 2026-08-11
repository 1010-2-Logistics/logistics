package com.logistics.ai.infrastructure.feign.response;

public record UserInfoResponseDto(
		Long userId,
		String name,
		String slackId
) {

}
