package com.logistics.order.infrastructure.feign.client;


import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.config.FeignConfig;
import com.logistics.order.infrastructure.feign.request.DeliveryCreateRequest;
import com.logistics.order.infrastructure.feign.response.DeliveryCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "delivery-service",
        configuration = FeignConfig.class
)
public interface DeliveryClient {
    @PostMapping("/internal/v1/deliveries")
    ApiResponse<DeliveryCreateResponse> createDelivery(
            @RequestBody DeliveryCreateRequest deliveryCreateRequest
    );

    @PatchMapping("/internal/v1/deliveries/order/{orderId}/cancel")
    ApiResponse<Void> cancelDelivery(
            @PathVariable("orderId") UUID orderId
    );
}
