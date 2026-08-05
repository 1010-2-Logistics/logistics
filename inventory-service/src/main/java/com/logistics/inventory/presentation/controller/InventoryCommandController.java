package com.logistics.inventory.presentation.controller;

import com.logistics.inventory.application.dto.command.InventoryCreateCommand;
import com.logistics.inventory.application.dto.command.InventoryUpdateCommand;
import com.logistics.inventory.application.dto.result.InventoryCreateResult;
import com.logistics.inventory.application.dto.result.InventoryUpdateResult;
import com.logistics.inventory.application.facade.InventoryFacade;
import com.logistics.inventory.application.service.InventoryCommandService;
import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.presentation.dto.request.InventoryCreateRequestDto;
import com.logistics.inventory.presentation.dto.request.InventoryUpdateRequestDto;
import com.logistics.inventory.presentation.dto.response.InventoryCreateResponseDto;
import com.logistics.inventory.presentation.dto.response.InventoryUpdateResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryCommandController {
    private final InventoryFacade inventoryFacade;
    private final InventoryCommandService inventoryCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InventoryCreateResponseDto> createInventory(
            @Valid @RequestBody InventoryCreateRequestDto inventoryCreateRequestDto
    ) {
        InventoryCreateCommand createInventoryCommand = InventoryCreateCommand.toCommand(inventoryCreateRequestDto);

        InventoryCreateResult inventoryCreateResultDto = inventoryFacade.createInventory(createInventoryCommand);

        InventoryCreateResponseDto inventoryCreateResponseDto = InventoryCreateResponseDto.from(inventoryCreateResultDto);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "재고 생성 성공",
                inventoryCreateResponseDto
        );
    }

    @PutMapping("/{inventoryId}")
    public ApiResponse<InventoryUpdateResponseDto> updateInventory(
            @PathVariable("inventoryId") UUID inventoryId,
            @Valid @RequestBody InventoryUpdateRequestDto inventoryUpdateRequestDto
    ) {
        InventoryUpdateCommand updateInventoryCommand = InventoryUpdateCommand.from(
                inventoryId,
                inventoryUpdateRequestDto
        );

        InventoryUpdateResult inventoryUpdateResult = inventoryCommandService.updateInventory(updateInventoryCommand);

        InventoryUpdateResponseDto inventoryUpdateResponseDto = InventoryUpdateResponseDto.from(inventoryUpdateResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "재고 수정 성공",
                inventoryUpdateResponseDto
        );
    }

    @DeleteMapping("/{inventoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("inventoryId") UUID inventoryId) {
        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
        inventoryCommandService.deleteInventory(inventoryId, "system");
    }
}
