package com.logistics.order.infrastructure.feign.client;

import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.config.FeignConfig;
import com.logistics.order.infrastructure.feign.request.InventoryReserveRequest;
import com.logistics.order.infrastructure.feign.response.InventoryReserveResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "inventory-service",
        configuration = FeignConfig.class
)
public interface InventoryClient {
    @PostMapping("/internal/v1/inventories/reserve")
    ApiResponse<InventoryReserveResponse> reserveInventory(
            @RequestBody InventoryReserveRequest inventoryReserveRequest
    );
}
