package com.logistics.inventory.presentation.controller;

import com.logistics.inventory.application.dto.command.InventoryCreateCommand;
import com.logistics.inventory.application.dto.command.InventoryUpdateCommand;
import com.logistics.inventory.application.dto.result.InventoryCreateResult;
import com.logistics.inventory.application.facade.InventoryFacade;
import com.logistics.inventory.application.service.InventoryCommandService;
import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.presentation.dto.request.InventoryCreateRequestDto;
import com.logistics.inventory.presentation.dto.request.InventoryUpdateRequestDto;
import com.logistics.inventory.presentation.dto.response.InventoryCreateResponseDto;
import com.logistics.inventory.presentation.dto.response.InventoryUpdateResponseDto;
import jakarta.validation.Valid;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
        InventoryCreateCommand createInventoryCommand = new InventoryCreateCommand(
                inventoryCreateRequestDto.productId(),
                inventoryCreateRequestDto.hubId(),
                inventoryCreateRequestDto.stock()
        );

        InventoryCreateResult inventoryCreateResultDto = inventoryFacade.createInventory(createInventoryCommand);

        InventoryCreateResponseDto inventoryCreateResponseDto = new InventoryCreateResponseDto(inventoryCreateResultDto.inventoryId());

        return ApiResponse.success(
                201,
                "재고 생성 성공",
                inventoryCreateResponseDto
        );
    }

    @PutMapping("/{inventoryId}")
    public ApiResponse<InventoryUpdateResponseDto> update(
            @PathVariable UUID inventoryId,
            @Valid @RequestBody InventoryUpdateRequestDto inventoryUpdateRequestDto
    ) {
        InventoryUpdateCommand updateInventoryCommand = new InventoryUpdateCommand();

        inventoryCommandService.updateInventory(updateInventoryCommand);

        InventoryUpdateResponseDto inventoryUpdateResponseDto = new InventoryUpdateResponseDto();

        return ApiResponse.success(
                200,
                "재고 수정 성공",
                inventoryUpdateResponseDto
        );
    }

    @DeleteMapping("/{inventoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID inventoryId) {
        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
        inventoryCommandService.deleteInventory(inventoryId, "system");
    }
}
