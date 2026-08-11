package com.logistics.ai.infrastructure.feign.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.logistics.ai.global.response.ApiResponse;
import com.logistics.ai.infrastructure.config.FeignConfig;
import com.logistics.ai.infrastructure.feign.response.ProductInfoResponseDto;

@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductClient {

	@GetMapping("/internal/v1/products/{productId}")
	public ApiResponse<ProductInfoResponseDto> getProductInfo(UUID productId);
}
