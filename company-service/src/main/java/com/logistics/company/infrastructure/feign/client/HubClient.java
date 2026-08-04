package com.logistics.company.infrastructure.feign.client;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.logistics.company.infrastructure.config.FeignConfig;
import com.logistics.company.infrastructure.feign.response.HubValidationResponse;

// name은 Eureka에 등록된 대상 서비스의 spring.application.name과 일치해야 합니다.
@FeignClient(name = "hub-service", configuration = FeignConfig.class)
public interface HubClient {

    @GetMapping("/internal/hubs/{hubId}")
    HubValidationResponse getHub(@PathVariable("hubId") UUID hubId);
}
