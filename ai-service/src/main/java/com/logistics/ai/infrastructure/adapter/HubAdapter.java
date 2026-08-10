package com.logistics.ai.infrastructure.adapter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.HubInfo;
import com.logistics.ai.application.port.out.HubPort;
import com.logistics.ai.infrastructure.feign.client.HubClient;
import com.logistics.ai.infrastructure.feign.response.HubInfoResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HubAdapter implements HubPort {

	private final HubClient hubClient;

	@Override
	public List<HubInfo> getHubInfo(Set<UUID> hubIds) {
		Set<HubInfoResponseDto> hubInfoList = hubClient.getHubInfoList(hubIds);
		
		return hubInfoList.stream()
				.map(HubInfoResponseDto::toApplication)
				.toList();
	}
}
