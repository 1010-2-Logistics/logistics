package com.logistics.hubRoute.infrastructure.feign.client;

import com.logistics.hubRoute.infrastructure.config.FeignConfig;
import com.logistics.hubRoute.infrastructure.feign.response.HubRouteValidationResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// name은 Eureka에 등록된 대상 서비스의 spring.application.name과 일치해야 합니다.
@FeignClient(name = "hub-service", configuration = FeignConfig.class)
public interface HubRouteClient {

    @GetMapping("/internal/v1/hubs/{hubId}")
    HubRouteValidationResponse getHub(@PathVariable("hubId") UUID hubId);
}
