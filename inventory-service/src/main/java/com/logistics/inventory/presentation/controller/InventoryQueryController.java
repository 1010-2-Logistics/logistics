package com.logistics.inventory.presentation.controller;

import com.logistics.inventory.application.dto.query.InventorySearchQuery;
import com.logistics.inventory.application.dto.result.InventoryDetailResult;
import com.logistics.inventory.application.dto.result.InventoryListResult;
import com.logistics.inventory.application.service.InventoryQueryService;
import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.presentation.dto.response.InventoryDetailResponseDto;
import com.logistics.inventory.presentation.dto.response.InventoryListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")

@RequiredArgsConstructor
public class InventoryQueryController {
    private final InventoryQueryService inventoryQueryService;

    @GetMapping("/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryDetailResponseDto>> getInventory(
            @PathVariable("inventoryId") UUID inventoryId
    ) {
        InventoryDetailResult getOneInventoryResult = inventoryQueryService.getInventory(inventoryId);

        InventoryDetailResponseDto getOneInventoryResponseDto = InventoryDetailResponseDto.from(getOneInventoryResult);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "재고 단건 조회 성공",
                getOneInventoryResponseDto
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<InventoryListResponseDto>> searchInventory(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        InventorySearchQuery inventorySearchQuery = new InventorySearchQuery(
                productId,
                hubId,
                sort,
                page,
                size
        );

        InventoryListResult inventoryListResult = inventoryQueryService.searchInventory(inventorySearchQuery);

        InventoryListResponseDto inventoryListResponseDto = InventoryListResponseDto.from(inventoryListResult);

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "재고 목록 조회 성공",
                inventoryListResponseDto
        ));
    }
}
