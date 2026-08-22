package com.logistics.product.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.logistics.product.global.response.ApiResponse;
import com.logistics.product.infrastructure.config.FeignConfig;
import com.logistics.product.infrastructure.feign.response.UserExistsClientResponseDto;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserClient {

	@GetMapping("/internal/v1/users/{userId}")
	ApiResponse<UserExistsClientResponseDto> getUserAuthentication(@PathVariable("userId") Long userId);
}
