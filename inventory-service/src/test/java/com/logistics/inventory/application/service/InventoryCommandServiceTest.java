package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.command.InventoryDeductionCommand;
import com.logistics.inventory.application.dto.result.InventoryDeductionResultDto;
import com.logistics.inventory.application.port.EventPublisher;
import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.domain.repository.InventoryCommandRepository;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.global.exception.InventoryErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class InventoryCommandServiceTest {
    UUID productId = UUID.randomUUID();
    UUID hubId = UUID.randomUUID();

    @Mock
    InventoryCommandRepository inventoryCommandRepository;

    @Mock
    EventPublisher eventPublisher;

    @InjectMocks
    InventoryCommandService inventoryCommandService;

    @Nested
    @DisplayName("재고 차감")
    class Deduct {
        @Test
        @DisplayName("재고 정상 차감 성공")
        void inventory_deduct_success() {
            Inventory inventory = Inventory.create(
                    productId,
                    hubId,
                    100
            );
            InventoryDeductionCommand inventoryDeductionCommand = new InventoryDeductionCommand(
                    productId,
                    hubId,
                    1
            );

            given(inventoryCommandRepository.findByProductAndHubId(productId, hubId)).willReturn(Optional.of(inventory));

            given(inventoryCommandRepository.save(inventory)).willReturn(inventory);

            InventoryDeductionResultDto inventoryDeductionResultDto = inventoryCommandService.deductInventory(inventoryDeductionCommand);

            assertThat(inventoryDeductionResultDto.stock()).isEqualTo(99);
            assertThat(inventory.getStock()).isEqualTo(99);

            verify(inventoryCommandRepository).save(inventory);
        }

        @Test
        @DisplayName("차감 수량이 현재 재고보다 많으면 예외 처리")
        void inventory_deduct_quantity_fail() {
            Inventory inventory = Inventory.create(
                    productId,
                    hubId,
                    100
            );
            assertThatThrownBy(() -> inventory.deduct(101))
                    .isInstanceOfSatisfying(CustomException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(InventoryErrorCode.INVENTORY_OUT_OF_STOCK));
        }

        @Test
        @DisplayName("차감 수량이 1보다 작으면 예외 처리")
        void inventory_deduct_1_fail() {
            Inventory inventory = Inventory.create(
                    productId,
                    hubId,
                    100
            );
            assertThatThrownBy(() -> inventory.deduct(0))
                    .isInstanceOfSatisfying(CustomException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(InventoryErrorCode.INVENTORY_INVALID_REQUEST));
        }
    }
}