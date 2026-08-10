package com.logistics.inventory.presentation.controller;


import com.logistics.inventory.application.dto.command.InventoryDeductionCommand;
import com.logistics.inventory.application.dto.command.InventoryRestorationCommand;
import com.logistics.inventory.application.dto.result.InventoryDeductionResult;
import com.logistics.inventory.application.dto.result.InventoryRestorationResult;
import com.logistics.inventory.application.service.InventoryCommandService;
import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.presentation.dto.request.InventoryDeductionRequestDto;
import com.logistics.inventory.presentation.dto.request.InventoryRestorationRequestDto;
import com.logistics.inventory.presentation.dto.response.InventoryDeductionResponseDto;
import com.logistics.inventory.presentation.dto.response.InventoryRestorationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name= "Inventory Internal")
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/inventories")
public class InventoryInternalCommandController {
    private final InventoryCommandService inventoryCommandService;

    @Operation(
            summary = "내부용 재고 차감"
    )
    @PostMapping("/deductions")
    public ResponseEntity<ApiResponse<InventoryDeductionResponseDto>> deductInventory(
            @Valid @RequestBody InventoryDeductionRequestDto inventoryDeductionRequestDto
    ) {
        InventoryDeductionCommand inventoryDeductionCommand = inventoryDeductionRequestDto.toCommand();

        InventoryDeductionResult inventoryDeductionResultDto = inventoryCommandService.deductInventory(inventoryDeductionCommand);

        InventoryDeductionResponseDto inventoryDeductionResponseDto = InventoryDeductionResponseDto.from(inventoryDeductionResultDto);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "재고 차감 성공",
                inventoryDeductionResponseDto
        ));
    }

    @Operation(
            summary = "내부용 재고 복원"
    )
    @PostMapping("/restorations")
    public ResponseEntity<ApiResponse<InventoryRestorationResponseDto>> restoreInventory(
            @Valid @RequestBody InventoryRestorationRequestDto inventoryRestorationRequestDto
    ) {
        InventoryRestorationCommand inventoryRestorationCommand = inventoryRestorationRequestDto.toCommand();
        InventoryRestorationResult inventoryRestorationResultDto = inventoryCommandService.restoreInventory(inventoryRestorationCommand);

        InventoryRestorationResponseDto inventoryRestorationResponseDto = InventoryRestorationResponseDto.from(inventoryRestorationResultDto);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "재고 복원 성공",
                inventoryRestorationResponseDto
        ));
    }
}
