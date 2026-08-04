package com.logistics.order.infrastructure.feign.client;

import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.config.FeignConfig;
import com.logistics.order.infrastructure.feign.response.CompanyOrderInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "company-service",
        configuration = FeignConfig.class
)
public interface CompanyClient {
    @GetMapping("/internal/v1/companies")
    ApiResponse<CompanyOrderInfoResponse> getCompaniesForOrder(
            @RequestParam UUID startCompanyId,
            @RequestParam UUID endCompanyId
    );
}
