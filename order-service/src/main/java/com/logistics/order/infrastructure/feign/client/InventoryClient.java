package com.logistics.order.infrastructure.feign.client;

import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.config.FeignConfig;
import com.logistics.order.infrastructure.feign.request.InventoryDeductionRequest;
import com.logistics.order.infrastructure.feign.request.InventoryRestorationRequest;
import com.logistics.order.infrastructure.feign.response.InventoryDeductionResponse;
import com.logistics.order.infrastructure.feign.response.InventoryRestorationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "inventory-service",
        configuration = FeignConfig.class
)
public interface InventoryClient {
    @PostMapping("/internal/v1/inventories/deductions")
    ApiResponse<InventoryDeductionResponse> deductInventory(
            @RequestBody InventoryDeductionRequest inventoryDeductionRequest
    );

    @PostMapping("/internal/v1/inventories/restorations")
    ApiResponse<InventoryRestorationResponse> restoreInventory(
            @RequestBody InventoryRestorationRequest inventoryRestorationRequest
    );
}
