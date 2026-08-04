package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.global.response.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public record InventoryListResult(
        List<InventorySummaryResult> content,
        PageInfo pageInfo
) {
    public static InventoryListResult from(
            Page<Inventory> inventories
    ) {
        List<InventorySummaryResult> content =
                inventories.getContent().stream()
                        .map(InventorySummaryResult::from)
                        .toList();

        return new InventoryListResult(
                content,
                PageInfo.of(inventories)
        );
    }
}
