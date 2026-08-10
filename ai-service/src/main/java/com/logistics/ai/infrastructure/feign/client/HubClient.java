package com.logistics.ai.infrastructure.feign.client;

import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.logistics.ai.infrastructure.config.FeignConfig;
import com.logistics.ai.infrastructure.feign.response.HubInfoResponseDto;

@FeignClient(name = "hub-service", configuration = FeignConfig.class)
public interface HubClient {

	@GetMapping("/internal/v1/hubs/info")
	Set<HubInfoResponseDto> getHubInfoList(Set<UUID> hubIds);

}
