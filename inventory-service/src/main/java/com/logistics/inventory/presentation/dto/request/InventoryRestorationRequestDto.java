package com.logistics.inventory.presentation.dto.request;

import com.logistics.inventory.application.dto.command.InventoryRestorationCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InventoryRestorationRequestDto(
        @NotNull(message = "상품 ID는 필수입니다.")
        UUID productId,

        @NotNull(message = "허브 ID는 필수입니다.")
        UUID hubId,

        @NotNull(message = "복원 수량은 필수입니다.")
        @Min(value = 1, message = "복원 수량은 1 이상이어야 합니다.")
        Integer quantity
) {
    public InventoryRestorationCommand toCommand() {
        return new InventoryRestorationCommand(
                productId,
                hubId,
                quantity()
        );
    }
}
