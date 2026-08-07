package com.logistics.product.infrastructure.feign.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.internal.response.CompanyExistsResponseDto;
import com.logistics.product.application.port.CompanyPort;
import com.logistics.product.infrastructure.feign.client.CompanyClient;
import com.logistics.product.infrastructure.feign.response.CompanyExistsClientResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyAdapter implements CompanyPort {
	
	private final CompanyClient companyClient;
	
	@Override
	public CompanyExistsResponseDto companyExistsRequest(UUID companyId) {
		CompanyExistsClientResponseDto response = companyClient.getCompanyInfo(companyId).getData();
		
		return new CompanyExistsResponseDto(
				response.companyId(),
				response.companyType(),
				response.companyName(),
				response.hubId(),
				response.companyManagerId(),
				response.exists()
		);
	}

	@Override
	public List<UUID> companyIdsByHubIdRequest(UUID hubID) {
		List<UUID> companyIds = companyClient.getCompanyIdsByHubId(hubID).getData();
		
		return companyIds;
	}

}
