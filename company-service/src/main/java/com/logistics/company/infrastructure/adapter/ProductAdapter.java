package com.logistics.company.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.company.application.dto.internal.request.CompanyNameUpdateRequestDto;
import com.logistics.company.application.dto.internal.response.CompanyNameUpdateResponseDto;
import com.logistics.company.application.port.CompanyCommandService;
import com.logistics.company.application.port.ProductPort;
import com.logistics.company.domain.entity.Company;
import com.logistics.company.infrastructure.feign.client.ProductClient;
import com.logistics.company.infrastructure.feign.response.CompanyNameUpdateClientResponseDto;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPort {

	private final CompanyCommandService commandService;
	
	private final ProductClient productClient;
	
	@Override
	public CompanyNameUpdateResponseDto companyNameUpdate(CompanyNameUpdateRequestDto request, String beforeName) {
		try {
			CompanyNameUpdateClientResponseDto response = productClient.companyNameUpdate(request).getData();
		
			return ProductAdapter.from(response);
		}
		
		catch (FeignException e) {
			Company company = commandService.updateFailCompany(request.companyId(), beforeName);
			
			return ProductAdapter.from(company);
		}
	}

	private static CompanyNameUpdateResponseDto from(Company company) {
		return new CompanyNameUpdateResponseDto(
				0, 0, company.getCompanyId(), false
		);
	}
	
	private static CompanyNameUpdateResponseDto from(CompanyNameUpdateClientResponseDto response) {
		return new CompanyNameUpdateResponseDto(
				response.productCount(),
				response.updateFailCount(),
				response.companyId(),
				response.exists()
		);
	}
}
