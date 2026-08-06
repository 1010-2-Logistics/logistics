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
import org.springframework.data.domain.Sort;
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
        int page = validatePage(searchInventoryQuery.page());
        int size = normalizeSize(searchInventoryQuery.size());
        String sortProperty = validateSort(searchInventoryQuery.sort());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sortProperty)
        );

        Page<Inventory> inventories = inventoryQueryRepository.search(
                searchInventoryQuery.productId(),
                searchInventoryQuery.hubId(),
                pageable
        );

        return InventoryListResult.from(inventories);
    }

    private int validatePage(Integer page) {
        if (page == null) {
            return 0;
        }

        if (page < 0) {
            throw new CustomException(InventoryErrorCode.INVENTORY_INVALID_REQUEST);
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return 10;
        }

        if (size == 10 || size == 30 || size == 50) {
            return size;
        }

        return 10;
    }

    private String validateSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "createdAt";
        }

        if ("createdAt".equals(sort) || "updatedAt".equals(sort)) {
            return sort;
        }

        throw new CustomException(InventoryErrorCode.INVENTORY_INVALID_REQUEST);
    }
}
