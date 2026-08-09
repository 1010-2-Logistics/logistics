package com.logistics.ai.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;

import com.logistics.ai.infrastructure.config.FeignConfig;

@FeignClient(name = "hub-service", configuration = FeignConfig.class)
public interface HubClient {

}
