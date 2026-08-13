package com.logistics.ai.infrastructure.feign.client;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.logistics.ai.infrastructure.config.FeignConfig;
import com.logistics.ai.infrastructure.feign.response.HubInfoResponseDto;

@FeignClient(name = "hub-service", configuration = FeignConfig.class)
public interface HubClient {

	// 어노테이션이 없으면 쿼리 파라미터로 전달되지 않고 요청 본문으로 취급된다
	@GetMapping("/internal/v1/hubs/getHubInfos")
	Set<HubInfoResponseDto> getHubInfoList(@RequestParam("hubIds") List<UUID> hubIds);

}
