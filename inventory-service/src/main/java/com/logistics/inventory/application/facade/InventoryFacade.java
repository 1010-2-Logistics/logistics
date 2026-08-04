package com.logistics.inventory.application.facade;

import com.logistics.inventory.application.dto.command.InventoryCreateCommand;
import com.logistics.inventory.application.dto.result.InventoryCreateResult;
import com.logistics.inventory.application.service.InventoryCommandService;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.global.exception.InventoryErrorCode;
import com.logistics.inventory.infrastructure.feign.client.HubClient;
import com.logistics.inventory.infrastructure.feign.client.ProductClient;
import com.logistics.inventory.infrastructure.feign.response.HubValidationResponse;
import com.logistics.inventory.infrastructure.feign.response.ProductValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryFacade {
    private final InventoryCommandService inventoryCommandService;
    private final HubClient hubClient;
    private final ProductClient productClient;

    public InventoryCreateResult createInventory(
            InventoryCreateCommand createInventoryCommand
    ) {
        ProductValidationResponse productValidationResponse = productClient.getProduct(createInventoryCommand.productId());

        if (!productValidationResponse.exists()) {
            throw new CustomException(InventoryErrorCode.INVENTORY_PRODUCT_NOT_FOUND);
        }

        HubValidationResponse hubValidationResponse = hubClient.getHub(createInventoryCommand.hubId());

        if (!hubValidationResponse.exists()) {
            throw new CustomException(InventoryErrorCode.INVENTORY_HUB_NOT_FOUND);
        }

        return inventoryCommandService.createInventory(createInventoryCommand);
    }
}
