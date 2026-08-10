package com.logistics.inventory.presentation.controller;

import com.logistics.inventory.application.dto.command.InventoryCreateCommand;
import com.logistics.inventory.application.dto.command.InventoryDeleteCommand;
import com.logistics.inventory.application.dto.command.InventoryUpdateCommand;
import com.logistics.inventory.application.dto.result.InventoryCreateResult;
import com.logistics.inventory.application.dto.result.InventoryUpdateResult;
import com.logistics.inventory.application.facade.InventoryFacade;
import com.logistics.inventory.application.service.InventoryCommandService;
import com.logistics.inventory.global.response.ApiResponse;
import com.logistics.inventory.infrastructure.security.principal.UserPrincipal;
import com.logistics.inventory.presentation.dto.request.InventoryCreateRequestDto;
import com.logistics.inventory.presentation.dto.request.InventoryUpdateRequestDto;
import com.logistics.inventory.presentation.dto.response.InventoryCreateResponseDto;
import com.logistics.inventory.presentation.dto.response.InventoryUpdateResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Inventory")
@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryCommandController {
    private final InventoryFacade inventoryFacade;
    private final InventoryCommandService inventoryCommandService;

    @Operation(
            summary = "재고 생성",
            description = """
                     접근 권한:
                    - MASTER: 모든 허브의 재고 생성 가능
                    - HUB_MANAGER: 본인이 담당하는 허브의 재고만 생성 가능
                    """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InventoryCreateResponseDto> createInventory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InventoryCreateRequestDto inventoryCreateRequestDto
    ) {
        InventoryCreateCommand createInventoryCommand = InventoryCreateCommand.toCommand(inventoryCreateRequestDto);

        InventoryCreateResult inventoryCreateResultDto = inventoryFacade.createInventory(
                createInventoryCommand,
                principal.toAuthenticatedUser()
        );

        InventoryCreateResponseDto inventoryCreateResponseDto = InventoryCreateResponseDto.from(inventoryCreateResultDto);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "재고 생성 성공",
                inventoryCreateResponseDto
        );
    }

    @Operation(
            summary = "재고 수정",
            description = """
                     접근 권한:
                    - MASTER : 모든 허브의 재고 수정 가능
                    - HUB_MANAGER : 담당 허브 재고만 수정 가능
                    """
    )
    @PutMapping("/{inventoryId}")
    public ApiResponse<InventoryUpdateResponseDto> updateInventory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("inventoryId") UUID inventoryId,
            @Valid @RequestBody InventoryUpdateRequestDto inventoryUpdateRequestDto
    ) {
        InventoryUpdateCommand updateInventoryCommand = InventoryUpdateCommand.from(
                inventoryId,
                inventoryUpdateRequestDto
        );

        InventoryUpdateResult inventoryUpdateResult = inventoryCommandService.updateInventory(
                updateInventoryCommand,
                principal.toAuthenticatedUser()
        );

        InventoryUpdateResponseDto inventoryUpdateResponseDto = InventoryUpdateResponseDto.from(inventoryUpdateResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "재고 수정 성공",
                inventoryUpdateResponseDto
        );
    }

    @Operation(
            summary = "재고 삭제",
            description = """
                     접근 권한:
                    - MASTER : 모든 허브의 재고 삭제 가능
                    - HUB_MANAGER : 담당 허브 재고만 삭제 가능
                    """
    )
    @DeleteMapping("/{inventoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("inventoryId") UUID inventoryId
    ) {
        InventoryDeleteCommand inventoryDeleteCommand = new InventoryDeleteCommand(
                inventoryId,
                principal.toAuthenticatedUser()
        );

        // TODO: 인증 적용 후 실제 로그인 사용자 ID로 교체
        Long deletedBy = 1L;

        inventoryCommandService.deleteInventory(
                inventoryDeleteCommand,
                deletedBy
        );
    }
}
