package com.logistics.order.infrastructure.feign.client;


import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.config.FeignConfig;
import com.logistics.order.infrastructure.feign.request.DeliveryCreateRequest;
import com.logistics.order.infrastructure.feign.response.DeliveryCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "delivery-service",
        configuration = FeignConfig.class
)
public interface DeliveryClient {
    @PostMapping("/internal/v1/deliveries")
    ApiResponse<DeliveryCreateResponse> createDelivery(
            @RequestBody DeliveryCreateRequest deliveryCreateRequest
    );
}
