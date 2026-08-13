package com.logistics.ai.infrastructure.feign.response;

import java.util.UUID;

import com.logistics.ai.application.dto.internal.HubInfo;

public record HubInfoResponseDto(
		UUID hubId,
		String name,
		String hubAddress
) {
	public HubInfo toApplication() {
		return new HubInfo(
				hubId,
				name,
				hubAddress
		);
	}
}
