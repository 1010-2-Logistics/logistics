package com.logistics.product.infrastructure.feign.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.infrastructure.config.FeignConfig;
import com.logistics.product.infrastructure.feign.response.CompanyExistsClientResponseDto;

@FeignClient(name = "company-service", configuration = FeignConfig.class)
public interface CompanyClient {

	@GetMapping("/internal/v1/companies/{companyId}/exists")
	ApiResponse<CompanyExistsClientResponseDto> getCompanyInfo(@PathVariable("companyId") UUID companyId);
	
	@GetMapping("/internal/v1/companies/ids/{hubId}")
	ApiResponse<List<UUID>> getCompanyIdsByHubId(@PathVariable("hubId") UUID hubId);
}
