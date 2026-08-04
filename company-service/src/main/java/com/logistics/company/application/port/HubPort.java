package com.logistics.company.application.port;

import java.util.UUID;

import com.logistics.company.application.dto.internal.HubInfoResponseDto;

public interface HubPort {
	HubInfoResponseDto getHubInfo(UUID hubId);
}
