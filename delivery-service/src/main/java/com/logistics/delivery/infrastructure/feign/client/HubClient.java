package com.logistics.delivery.infrastructure.feign.client;

import com.logistics.delivery.infrastructure.config.FeignConfig;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hub-service", configuration = FeignConfig.class)
public interface HubClient {

    @GetMapping("/internal/v1/hubs")
    Set<UUID> validateHubIds(@RequestParam("hubIds") List<UUID> hubIds);
}