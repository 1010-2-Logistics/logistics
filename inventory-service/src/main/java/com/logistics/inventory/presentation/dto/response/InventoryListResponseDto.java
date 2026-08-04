package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryListResult;
import com.logistics.inventory.global.response.PageInfo;

import java.util.List;

public record InventoryListResponseDto(
        List<InventorySummaryResponseDto> content,
        PageInfo pageInfo
) {
    public static InventoryListResponseDto from(
            InventoryListResult result
    ) {
        List<InventorySummaryResponseDto> content =
                result.content().stream()
                        .map(InventorySummaryResponseDto::from)
                        .toList();

        return new InventoryListResponseDto(
                content,
                result.pageInfo()
        );
    }
}
