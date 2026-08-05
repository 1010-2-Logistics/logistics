package com.logistics.hubRoute.infrastructure.feign.client;

import com.logistics.hubRoute.infrastructure.config.FeignConfig;
import com.logistics.hubRoute.infrastructure.feign.response.HubRouteValidationResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

// hub-service로 할 경우 유레카에서 오류 나서 대문자로 변경해서 진행
@FeignClient(name = "HUB-SERVICE", configuration = FeignConfig.class)
public interface HubClient {

    @GetMapping("/internal/v1/hubs/{hubId}")
    boolean get(@PathVariable("hubId") UUID hubId);


    @GetMapping("/internal/v1/hubs")
    Set<UUID> validateHubIds(@RequestParam("hubIds") List<UUID> hubIds);
}
