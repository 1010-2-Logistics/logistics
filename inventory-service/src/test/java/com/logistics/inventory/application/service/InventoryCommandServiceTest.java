package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.command.*;
import com.logistics.inventory.application.dto.result.InventoryCreateResult;
import com.logistics.inventory.application.dto.result.InventoryDeductionResult;
import com.logistics.inventory.application.dto.result.InventoryRestorationResult;
import com.logistics.inventory.application.dto.result.InventoryUpdateResult;
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

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class InventoryCommandServiceTest {
    UUID productId = UUID.randomUUID();
    UUID hubId = UUID.randomUUID();
    UUID inventoryId = UUID.randomUUID();

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

            InventoryDeductionResult inventoryDeductionResultDto = inventoryCommandService.deductInventory(inventoryDeductionCommand);

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

    @Nested
    @DisplayName("재고 복원")
    class Restore {
        @Test
        @DisplayName("재고 정상 복원 성공")
        void inventory_restore_success() {
            Inventory inventory = Inventory.create(
                    productId,
                    hubId,
                    100
            );

            InventoryRestorationCommand inventoryRestorationCommand = new InventoryRestorationCommand(
                    productId,
                    hubId,
                    30
            );

            given(inventoryCommandRepository.findByProductAndHubId(productId, hubId)).willReturn(Optional.of(inventory));
            given(inventoryCommandRepository.save(inventory)).willReturn(inventory);

            InventoryRestorationResult inventoryRestorationResultDto = inventoryCommandService.restoreInventory(inventoryRestorationCommand);

            assertThat(inventoryRestorationResultDto.stock()).isEqualTo(130);
            assertThat(inventory.getStock()).isEqualTo(130);

            verify(inventoryCommandRepository).findByProductAndHubId(
                    productId,
                    hubId
            );
            verify(inventoryCommandRepository).save(inventory);
        }

        @Test
        @DisplayName("복원할 재고 없으면 예외 처리")
        void inventory_restore_not_found() {
            InventoryRestorationCommand command =
                    new InventoryRestorationCommand(
                            productId,
                            hubId,
                            30
                    );

            given(inventoryCommandRepository.findByProductAndHubId(productId, hubId)).willReturn(Optional.empty());

            assertThatThrownBy(
                    () -> inventoryCommandService.restoreInventory(command)
            ).isInstanceOfSatisfying(
                    CustomException.class,
                    exception -> assertThat(exception.getErrorCode())
                            .isEqualTo(InventoryErrorCode.INVENTORY_NOT_FOUND)
            );

            verify(inventoryCommandRepository, never()).save(any(Inventory.class));
        }
    }

    @Nested
    @DisplayName("재고 생성")
    class create {
        @Test
        @DisplayName("재고 생성 성공")
        void inventory_create_success() {
            InventoryCreateCommand inventoryCreateCommand = new InventoryCreateCommand(
                    productId,
                    hubId,
                    100
            );
            Inventory savedInventory = mock(Inventory.class);
            given(inventoryCommandRepository.findByProductIdAndHubIdAndDeletedAtIsNull(
                    productId,
                    hubId
            )).willReturn(Optional.empty());
            given(savedInventory.getInventoryId()).willReturn(inventoryId);
            given(inventoryCommandRepository.save(any(Inventory.class))).willReturn(savedInventory);

            InventoryCreateResult inventoryCreateResult = inventoryCommandService.createInventory(
                    inventoryCreateCommand
            );

            assertThat(inventoryCreateResult.inventoryId()).isEqualTo(inventoryId);

            verify(inventoryCommandRepository).findByProductIdAndHubIdAndDeletedAtIsNull(
                    productId,
                    hubId
            );
            verify(inventoryCommandRepository).save(any(Inventory.class));
        }

        @Test
        @DisplayName("동일한 상품과 허브의 재고가 이미 존재하면 예외 처리")
        void inventory_create_duplicate_fail() {
            Inventory existingInventory = Inventory.create(
                    productId,
                    hubId,
                    100
            );
            InventoryCreateCommand inventoryCreateCommand = new InventoryCreateCommand(
                    productId,
                    hubId,
                    100
            );
            given(inventoryCommandRepository.findByProductIdAndHubIdAndDeletedAtIsNull(
                    productId,
                    hubId
            )).willReturn(Optional.of(existingInventory));

            assertThatThrownBy(() -> inventoryCommandService.createInventory(inventoryCreateCommand))
                    .isInstanceOfSatisfying(
                            CustomException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(InventoryErrorCode.INVENTORY_ALREADY_EXISTS)
                    );

            verify(inventoryCommandRepository, never()).save(any(Inventory.class));
        }

        @Test
        @DisplayName("같은 허브라도 상품이 다르면 재고 생성 성공")
        void inventory_create_different_product_success() {
            UUID anotherProductId = UUID.randomUUID();
            InventoryCreateCommand inventoryCreateCommand = new InventoryCreateCommand(
                    anotherProductId,
                    hubId,
                    100
            );
            Inventory savedInventory = mock(Inventory.class);

            given(inventoryCommandRepository.findByProductIdAndHubIdAndDeletedAtIsNull(
                    anotherProductId,
                    hubId
            )).willReturn(Optional.empty());
            given(inventoryCommandRepository.save(any(Inventory.class))).willReturn(savedInventory);
            given(savedInventory.getInventoryId()).willReturn(inventoryId);

            InventoryCreateResult inventoryCreateResult = inventoryCommandService.createInventory(inventoryCreateCommand);

            assertThat(inventoryCreateResult.inventoryId()).isEqualTo(inventoryId);

            verify(inventoryCommandRepository).findByProductIdAndHubIdAndDeletedAtIsNull(
                    anotherProductId,
                    hubId
            );
            verify(inventoryCommandRepository).save(any(Inventory.class));
        }
    }

    @Nested
    @DisplayName("재고 수정")
    class update {
        @Test
        @DisplayName("재고 수정 성공")
        void inventory_update_success() {
            InventoryUpdateCommand inventoryUpdateCommand = new InventoryUpdateCommand(inventoryId, 100);
            Inventory inventory = Inventory.create(
                    productId,
                    hubId,
                    50
            );

            given(inventoryCommandRepository.findByIdAndDeletedAtIsNull(inventoryId)).willReturn(Optional.of(inventory));
            given(inventoryCommandRepository.save(inventory)).willReturn(inventory);

            InventoryUpdateResult inventoryUpdateResult = inventoryCommandService.updateInventory(inventoryUpdateCommand);

            assertThat(inventoryUpdateResult).isNotNull();
            assertThat(inventory.getStock()).isEqualTo(100);

            verify(inventoryCommandRepository).findByIdAndDeletedAtIsNull(inventoryId);
            verify(inventoryCommandRepository).save(inventory);
        }

        @Nested
        @DisplayName("재고 삭제")
        class delete {
            @Test
            @DisplayName("재고 삭세 성공")
            void inventory_delete_success() {
                Long deletedBy = 1L;

                InventoryDeleteCommand inventoryDeleteCommand = new InventoryDeleteCommand(inventoryId);

                Inventory inventory = Inventory.create(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        50
                );

                given(inventoryCommandRepository.findByIdAndDeletedAtIsNull(inventoryId)).willReturn(Optional.of(inventory));
                given(inventoryCommandRepository.save(inventory)).willReturn(inventory);

                inventoryCommandService.deleteInventory(
                        inventoryDeleteCommand,
                        deletedBy
                );

                assertThat(inventory.getDeletedAt()).isNotNull();
                assertThat(inventory.getDeletedBy()).isEqualTo(deletedBy);

                verify(inventoryCommandRepository).findByIdAndDeletedAtIsNull(inventoryId);
                verify(inventoryCommandRepository).save(inventory);
            }
        }
    }
}