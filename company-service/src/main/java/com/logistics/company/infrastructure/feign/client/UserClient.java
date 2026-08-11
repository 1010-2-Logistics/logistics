package com.logistics.company.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.logistics.company.global.response.ApiResponse;
import com.logistics.company.infrastructure.config.FeignConfig;
import com.logistics.company.infrastructure.feign.request.UserRoleUpdateClientRequestDto;
import com.logistics.company.infrastructure.feign.response.UserExistsClientResponseDto;
import com.logistics.company.infrastructure.feign.response.UserRoleUpdateClientResponseDto;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserClient {

	@PatchMapping("/internal/v1/users/${userId}/affiliation")
	ApiResponse<UserRoleUpdateClientResponseDto> compnayManagerRoleUpdateRequest(
			@PathVariable("userId") Long userId,
			UserRoleUpdateClientRequestDto request
	);
	
	@GetMapping("/internal/v1/users/{userId}/exists")
	ApiResponse<UserExistsClientResponseDto> userExistsRequest(@PathVariable("userId") Long userId);
	
	@GetMapping("/internal/v1/users/{userId}")
	ApiResponse<?> getUserAuthentication(@PathVariable("userId") Long userId);
}
