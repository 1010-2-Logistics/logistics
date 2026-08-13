package com.logistics.ai.infrastructure.feign.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.logistics.ai.global.response.ApiResponse;
import com.logistics.ai.infrastructure.config.FeignConfig;
import com.logistics.ai.infrastructure.feign.response.ProductInfoResponseDto;

@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductClient {

	// 어노테이션이 없으면 경로 변수로 치환되지 않고 요청 본문으로 취급된다
	@GetMapping("/internal/v1/products/{productId}")
	public ApiResponse<ProductInfoResponseDto> getProductInfo(@PathVariable("productId") UUID productId);
}
