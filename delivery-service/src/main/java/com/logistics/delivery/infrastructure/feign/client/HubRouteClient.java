package com.logistics.delivery.infrastructure.feign.client;

import com.logistics.delivery.infrastructure.config.FeignConfig;
import com.logistics.delivery.infrastructure.feign.request.HubRouteFindRequest;
import com.logistics.delivery.infrastructure.feign.response.HubRoutePathApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "HubRoute-service", configuration = FeignConfig.class)
public interface HubRouteClient {

    @GetMapping("/api/v1/hubRoute/findHubRoute")
    HubRoutePathApiResponse findHubRoute(@RequestBody HubRouteFindRequest request);
}