package com.logistics.inventory.presentation.dto.response;

import com.logistics.inventory.application.dto.result.InventoryListResult;
import com.logistics.inventory.global.response.PageInfo;

import java.util.List;

public record InventoryListResponseDto(
        List<InventoryListItemResponseDto> content,
        PageInfo pageInfo
) {
    public static InventoryListResponseDto from(
            InventoryListResult result
    ) {
        List<InventoryListItemResponseDto> content =
                result.content().stream()
                        .map(InventoryListItemResponseDto::from)
                        .toList();

        return new InventoryListResponseDto(
                content,
                result.pageInfo()
        );
    }
}
