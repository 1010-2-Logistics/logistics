package com.logistics.ai.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.logistics.ai.global.response.ApiResponse;
import com.logistics.ai.infrastructure.config.FeignConfig;
import com.logistics.ai.infrastructure.feign.response.UserInfoResponseDto;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserClient {

	@GetMapping("/internal/v1/users/{userId}")
	ApiResponse<UserInfoResponseDto> getUserInfo(@PathVariable("userId") Long userId);
}
