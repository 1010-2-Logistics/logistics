package com.logistics.company.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.logistics.company.application.dto.internal.request.CompanyNameUpdateRequestDto;
import com.logistics.company.global.response.ApiResponse;
import com.logistics.company.infrastructure.config.FeignConfig;
import com.logistics.company.infrastructure.feign.response.CompanyNameUpdateClientResponseDto;

@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductClient {

	@PatchMapping("/internal/v1/products")
	public ApiResponse<CompanyNameUpdateClientResponseDto> companyNameUpdate(
			@RequestBody CompanyNameUpdateRequestDto request
	);
}
