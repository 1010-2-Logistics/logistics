package com.logistics.inventory.presentation.controller;

import com.logistics.inventory.application.dto.query.GetOneInventoryQuery;
import com.logistics.inventory.application.dto.query.SearchInventoryQuery;
import com.logistics.inventory.application.dto.result.InventoryGetOneResultDto;
import com.logistics.inventory.application.dto.result.InventorySummaryResultDto;
import com.logistics.inventory.application.service.InventoryQueryService;
import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.global.response.PageResponse;

import java.util.UUID;

import com.logistics.inventory.presentation.dto.response.InventoryGetOneResponseDto;
import com.logistics.inventory.presentation.dto.response.InventorySummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories")

@RequiredArgsConstructor
public class InventoryQueryController {

    private final InventoryQueryService inventoryQueryService;

    @GetMapping("/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryGetOneResponseDto>> getInventory(
            @PathVariable UUID inventoryId
    ) {
        GetOneInventoryQuery getOneInventoryQuery = new GetOneInventoryQuery();

        InventoryGetOneResultDto getOneInventoryResult = inventoryQueryService.getInventory();

        InventoryGetOneResponseDto getOneInventoryResponseDto = new InventoryGetOneResponseDto();

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "재고 단건 조회 성공",
                getOneInventoryResponseDto
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventorySummaryResponseDto>>> searchInventory(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID hubId,
            Pageable pageable
    ) {
        SearchInventoryQuery query = new SearchInventoryQuery(
                productId,
                hubId,
                pageable
        );

        Page<InventorySummaryResultDto> resultPage = inventoryQueryService.searchInventory(query);

        Page<InventorySummaryResponseDto> responsePage = null;

        return ResponseEntity.ok(ApiResponse.success(
                200,
                "재고 목록 조회 성공",
                PageResponse.of(responsePage)
        ));
    }
}
