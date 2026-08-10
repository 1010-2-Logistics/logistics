package com.logistics.ai.infrastructure.feign.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;

import com.logistics.ai.global.response.ApiResponse;
import com.logistics.ai.infrastructure.config.FeignConfig;
import com.logistics.ai.infrastructure.feign.response.DeliveryRouteListResponseDto;

@FeignClient(name = "delivery-service", configuration = FeignConfig.class)
public interface DeliveryClient {

	@GetMapping("/internal/v1/deliveries/{deliveryId}/routes")
	public ApiResponse<DeliveryRouteListResponseDto> getRoutes(@Param("deliveryId") UUID deliveryId);
}
