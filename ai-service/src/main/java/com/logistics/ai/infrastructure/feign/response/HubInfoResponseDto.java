package com.logistics.ai.infrastructure.feign.response;

import java.util.UUID;

import com.logistics.ai.application.dto.internal.HubInfo;

public record HubInfoResponseDto(
		UUID hubId,
		String hubName,
		String hubAddress
) {
	public HubInfo toApplication() {
		return new HubInfo(
				hubId,
				hubName,
				hubAddress
		);
	}
}
