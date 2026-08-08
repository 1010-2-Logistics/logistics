package com.logistics.ai.infrastructure.feign.client;

import org.springframework.cloud.openfeign.FeignClient;

import com.logistics.ai.infrastructure.config.FeignConfig;

@FeignClient(name = "delivery-service", configuration = FeignConfig.class)
public interface DeliveryRouteClient {

}
