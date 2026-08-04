package com.logistics.inventory.infrastructure.feign.client;


import com.logistics.inventory.infrastructure.config.FeignConfig;
import com.logistics.inventory.infrastructure.feign.response.ProductValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "product-service",
        configuration = FeignConfig.class
)
public interface ProductClient {
    @GetMapping("/internal/v1/products/{productId}/exists")
    ProductValidationResponse getProduct(
            @PathVariable("productId") UUID productId
    );
}
