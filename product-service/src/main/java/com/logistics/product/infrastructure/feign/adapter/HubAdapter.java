package com.logistics.product.infrastructure.feign.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.internal.response.HubInfoResponseDto;
import com.logistics.product.application.port.HubPort;
import com.logistics.product.infrastructure.feign.client.HubClient;
import com.logistics.product.infrastructure.feign.response.HubValidationResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HubAdapter implements HubPort {
	
	private final HubClient hubClient;
	
	@Override
	public HubInfoResponseDto getHubInfo(UUID hubId) {
		HubValidationResponse response = hubClient.getHub(hubId).getData();
		
		return new HubInfoResponseDto(
				response.hubId(),
				response.hubName()
		);
	}
}
