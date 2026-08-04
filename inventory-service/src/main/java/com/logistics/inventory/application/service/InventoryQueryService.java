package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.query.SearchInventoryQuery;
import com.logistics.inventory.application.dto.result.InventoryGetOneResult;
import com.logistics.inventory.application.dto.result.InventoryListResult;
import com.logistics.inventory.application.dto.result.InventorySummaryResult;
import com.logistics.inventory.domain.repository.InventoryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryQueryService {

    private final InventoryQueryRepository inventoryQueryRepository;

    public InventoryGetOneResult getInventory(
            UUID inventoryId
    ) {
        return null;
    }

    public InventoryListResult searchInventory(
            SearchInventoryQuery searchInventoryQuery
    ) {
        return null;
    }
}
