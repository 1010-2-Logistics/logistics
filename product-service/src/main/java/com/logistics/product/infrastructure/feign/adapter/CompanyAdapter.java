package com.logistics.product.infrastructure.feign.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.internal.CompanyExistsResponseDto;
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
				response.hubId(),
				response.companyManagerId(),
				response.exists()
		);
	}

}
