package com.logistics.inventory.application.dto.result;

import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.global.response.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public record InventoryListResult(
        List<InventoryListItemResult> content,
        PageInfo pageInfo
) {
    public static InventoryListResult from(
            Page<Inventory> inventories
    ) {
        List<InventoryListItemResult> content =
                inventories.getContent().stream()
                        .map(InventoryListItemResult::from)
                        .toList();

        return new InventoryListResult(
                content,
                PageInfo.of(inventories)
        );
    }
}
