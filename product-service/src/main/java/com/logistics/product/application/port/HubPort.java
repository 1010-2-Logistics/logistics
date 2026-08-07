package com.logistics.product.application.port;

import java.util.UUID;

import com.logistics.product.application.dto.internal.response.HubInfoResponseDto;

public interface HubPort {
	HubInfoResponseDto getHubInfo(UUID hubId);
}
