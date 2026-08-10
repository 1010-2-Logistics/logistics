package com.logistics.inventory.infrastructure.feign.client;

import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.infrastructure.config.FeignConfig;
import com.logistics.inventory.infrastructure.feign.response.HubValidationResponse;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "hub-service",
        configuration = FeignConfig.class)
public interface HubClient {

    @GetMapping("/internal/v1/hubs/{hubId}")
    ApiResponse<HubValidationResponse> getHub(@PathVariable("hubId") UUID hubId);
}
