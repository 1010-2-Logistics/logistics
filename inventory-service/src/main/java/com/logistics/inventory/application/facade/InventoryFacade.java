package com.logistics.inventory.application.facade;

import com.logistics.inventory.application.dto.command.InventoryCreateCommand;
import com.logistics.inventory.application.dto.result.HubExistsResponseDto;
import com.logistics.inventory.application.dto.result.ProductExistsResponseDto;
import com.logistics.inventory.application.dto.result.InventoryCreateResult;
import com.logistics.inventory.application.port.HubPort;
import com.logistics.inventory.application.port.ProductPort;
import com.logistics.inventory.application.service.InventoryCommandService;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.global.exception.InventoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryFacade {
    private final InventoryCommandService inventoryCommandService;
    private final HubPort hubPort;
    private final ProductPort productPort;

    public InventoryCreateResult createInventory(
            InventoryCreateCommand createInventoryCommand
    ) {
        ProductExistsResponseDto productExistsResponseDto = productPort.getProduct(createInventoryCommand.productId());

        if (!productExistsResponseDto.exists()) {
            throw new CustomException(InventoryErrorCode.INVENTORY_PRODUCT_NOT_FOUND);
        }

        HubExistsResponseDto hubExistsResponseDto = hubPort.getHub(createInventoryCommand.hubId());

        if (!hubExistsResponseDto.exists()) {
            throw new CustomException(InventoryErrorCode.INVENTORY_HUB_NOT_FOUND);
        }

        return inventoryCommandService.createInventory(createInventoryCommand);
    }
}
