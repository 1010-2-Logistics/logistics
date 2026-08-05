package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.query.InventorySearchQuery;
import com.logistics.inventory.application.dto.result.InventoryDetailResult;
import com.logistics.inventory.application.dto.result.InventoryListResult;
import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.domain.repository.InventoryQueryRepository;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.global.exception.InventoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryQueryService {

    private final InventoryQueryRepository inventoryQueryRepository;

    public InventoryDetailResult getInventory(
            UUID inventoryId
    ) {
        Inventory inventory = inventoryQueryRepository
                .findByInventoryIdAndDeletedAtIsNull(inventoryId)
                .orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        return InventoryDetailResult.from(inventory);
    }

    public InventoryListResult searchInventory(
            InventorySearchQuery searchInventoryQuery
    ) {
        Pageable pageable = PageRequest.of(

        );

        Page<Inventory> inventories = inventoryQueryRepository.search(

        );

        return InventoryListResult.from(inventories);
    }
}
