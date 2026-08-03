package com.logistics.inventory.presentation.controller;


import com.logistics.inventory.application.dto.command.InventoryDeductionCommand;
import com.logistics.inventory.application.dto.result.InventoryDeductionResultDto;
import com.logistics.inventory.application.service.InventoryCommandService;
import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.presentation.dto.request.InventoryDeductionRequestDto;
import com.logistics.inventory.presentation.dto.response.InventoryDeductionResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/inventories")
public class InventoryInternalCommandController {
    private final InventoryCommandService inventoryCommandService;

    @PostMapping("/deductions")
    public ResponseEntity<ApiResponse<InventoryDeductionResponseDto>> deductInventory(
            @Valid @RequestBody InventoryDeductionRequestDto inventoryDeductionsRequestDto
    ) {
        InventoryDeductionCommand inventoryDeductionCommand = inventoryDeductionsRequestDto.toCommand();

        InventoryDeductionResultDto inventoryDeductionResultDto = inventoryCommandService.deductInventory(inventoryDeductionCommand);

        InventoryDeductionResponseDto inventoryDeductionResponseDto = InventoryDeductionResponseDto.from(inventoryDeductionResultDto);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "재고 차감 성공",
                inventoryDeductionResponseDto
        ));
    }
}
