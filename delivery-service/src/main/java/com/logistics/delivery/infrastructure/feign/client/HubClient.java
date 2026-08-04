package com.logistics.delivery.infrastructure.feign.client;

import com.logistics.delivery.infrastructure.config.FeignConfig;
import com.logistics.delivery.infrastructure.feign.response.HubValidationResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub-service", configuration = FeignConfig.class)
public interface HubClient {

    @GetMapping("/internal/hubs/{hubId}")
    HubValidationResponse getHub(@PathVariable("hubId") UUID hubId);
}
