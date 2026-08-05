package com.logistics.order.infrastructure.feign.client;

import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.config.FeignConfig;
import com.logistics.order.infrastructure.feign.response.ProductGetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "product-service",
        configuration = FeignConfig.class
)
public interface ProductClient {
    @GetMapping("/internal/v1/products/{productId}")
    ApiResponse<ProductGetResponse> getProduct(
            @PathVariable UUID productId
    );
}
