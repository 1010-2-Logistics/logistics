package com.logistics.ai.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;

import com.logistics.ai.infrastructure.config.FeignConfig;

@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductClient {

}
