package com.logistics.inventory.presentation.controller;

import com.logistics.inventory.application.dto.query.InventorySearchQuery;
import com.logistics.inventory.application.dto.result.InventoryDetailResult;
import com.logistics.inventory.application.dto.result.InventoryListResult;
import com.logistics.inventory.application.service.InventoryQueryService;
import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.presentation.dto.request.InventorySearchRequest;
import com.logistics.inventory.presentation.dto.response.InventoryDetailResponseDto;
import com.logistics.inventory.presentation.dto.response.InventoryListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Tag(name= "Inventory")
@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryQueryController {
    private final InventoryQueryService inventoryQueryService;

    @Operation(
            summary = "재고 상세 조회",
            description = """
                     접근 권한:
                    - MASTER : 모든 허브의 재고 단건 조회 가능
                    - HUB_MANAGER : 본인이 담당하는 허브의 재고만 단건 조회 가능
                    - COMPANY_DELIVERY_MANAGER : 재고 단건 조회 가능
                    - COMPANY_MANAGER : 재고 단건 조회 가능
                    """
    )
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

    @Operation(
            summary = "재고 목록 조회",
            description = """
                     접근 권한:
                    - 모든 로그인 사용자
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<InventoryListResponseDto>> searchInventory(
           @ModelAttribute InventorySearchRequest inventorySearchRequest
    ) {
        InventorySearchQuery inventorySearchQuery = new InventorySearchQuery(
                inventorySearchRequest.productId(),
                inventorySearchRequest.hubId(),
                inventorySearchRequest.sort(),
                inventorySearchRequest.page(),
                inventorySearchRequest.size()
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
