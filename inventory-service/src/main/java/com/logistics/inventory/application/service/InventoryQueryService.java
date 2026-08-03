package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.query.SearchInventoryQuery;
import com.logistics.inventory.application.dto.result.InventoryGetOneResultDto;
import com.logistics.inventory.application.dto.result.InventorySummaryResultDto;
import com.logistics.inventory.domain.repository.InventoryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryQueryService {

    private final InventoryQueryRepository inventoryQueryRepository;

    public InventoryGetOneResultDto getInventory(
//            GetInventoryQuery getInventoryQuery
    ) {
        return null;
    }

    public Page<InventorySummaryResultDto> searchInventory(
            SearchInventoryQuery searchInventoryQuery
    ) {
        return null;
    }
}
