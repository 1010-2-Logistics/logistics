package com.logistics.company.infrastructure.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.company.application.dto.internal.response.HubInfoResponseDto;
import com.logistics.company.application.port.HubPort;
import com.logistics.company.global.exception.CompanyErrorCode;
import com.logistics.company.global.exception.CompanyException;
import com.logistics.company.infrastructure.feign.client.HubClient;
import com.logistics.company.infrastructure.feign.response.HubValidationResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HubAdapter implements HubPort {

	private final HubClient hubClient;
	
	@Override
	public HubInfoResponseDto getHubInfo(UUID hubId) {
		HubValidationResponse response = hubClient.getHubInfos(List.of(hubId)).stream()
				.findFirst()
				.orElseThrow(() -> new CompanyException(CompanyErrorCode.COMPANY_HUB_NOT_FOUND));
		
		return new HubInfoResponseDto(
				response.hubId(),
				response.name()
		);
	}

	
}
