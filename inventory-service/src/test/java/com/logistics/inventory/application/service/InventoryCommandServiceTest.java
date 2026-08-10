package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.command.*;
import com.logistics.inventory.application.dto.result.InventoryCreateResult;
import com.logistics.inventory.application.dto.result.InventoryDeductionResult;
import com.logistics.inventory.application.dto.result.InventoryRestorationResult;
import com.logistics.inventory.application.dto.result.InventoryUpdateResult;
import com.logistics.inventory.application.port.EventPublisher;
import com.logistics.inventory.application.port.IdempotencyPort;
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

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class InventoryCommandServiceTest {
    UUID productId = UUID.randomUUID();
    UUID hubId = UUID.randomUUID();
    UUID inventoryId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    Long deletedBy = 1L;

    @Mock
    InventoryCommandRepository inventoryCommandRepository;

    @Mock
    EventPublisher eventPublisher;

    @InjectMocks
    InventoryCommandService inventoryCommandService;

    @Mock
    IdempotencyPort idempotencyPort;

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
                    orderId,
                    productId,
                    hubId,
                    1
            );

            given(idempotencyPort.acquire(
                    eq("inventory:deduct:" + orderId),
                    any(Duration.class)
            )).willReturn(true);

            given(inventoryCommandRepository.findByProductAndHubIdWithLock(
                    productId,
                    hubId
            )).willReturn(Optional.of(inventory));

            given(inventoryCommandRepository.save(inventory)).willReturn(inventory);

            InventoryDeductionResult inventoryDeductionResultDto = inventoryCommandService.deductInventory(inventoryDeductionCommand);

            assertThat(inventoryDeductionResultDto.stock()).isEqualTo(99);
            assertThat(inventory.getStock()).isEqualTo(99);

            verify(inventoryCommandRepository).findByProductAndHubIdWithLock(
                    productId,
                    hubId
            );
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

        @Test
        @DisplayName("동일 주문의 재고 차감 요청이면 중복 처리 예외")
        void inventory_deduct_duplicate_fail() {
            InventoryDeductionCommand inventoryDeductionCommand = new InventoryDeductionCommand(
                    orderId,
                    productId,
                    hubId,
                    10
            );

            given(idempotencyPort.acquire(
                    eq("inventory:deduct:" + orderId),
                    any(Duration.class)
            )).willReturn(false);

            assertThatThrownBy(() -> inventoryCommandService.deductInventory(inventoryDeductionCommand))
                    .isInstanceOfSatisfying(CustomException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(InventoryErrorCode.INVENTORY_ALREADY_PROCESSED));

            verify(inventoryCommandRepository, never()).findByProductAndHubIdWithLock(any(), any());
            verify(inventoryCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("재고 차감 실패 시 멱등키 해제")
        void inventory_deduct_fail_release_key() {
            Inventory inventory = Inventory.create(
                    productId,
                    hubId,
                    5
            );

            InventoryDeductionCommand inventoryDeductionCommand = new InventoryDeductionCommand(
                    orderId,
                    productId,
                    hubId,
                    10
            );

            String key = "inventory:deduct:" + orderId;

            given(idempotencyPort.acquire(
                    eq(key),
                    any(Duration.class)
            )).willReturn(true);

            given(inventoryCommandRepository.findByProductAndHubIdWithLock(
                    productId,
                    hubId
            )).willReturn(Optional.of(inventory));

            assertThatThrownBy(() -> inventoryCommandService.deductInventory(inventoryDeductionCommand))
                    .isInstanceOf(CustomException.class);

            verify(idempotencyPort).release(key);
            verify(inventoryCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("동일 주문의 재고 차감 요청이면 이전 결과 반환")
        void inventory_deduct_duplicate_return_result() {
            InventoryDeductionCommand inventoryDeductionCommand = new InventoryDeductionCommand(
                    orderId,
                    productId,
                    hubId,
                    10
            );
            InventoryDeductionResult inventoryDeductionResult = new InventoryDeductionResult(
                    inventoryId,
                    productId,
                    90
            );

            given(idempotencyPort.getResult(
                    eq("inventory:deduct:" + orderId),
                    eq(InventoryDeductionResult.class)
            )).willReturn(Optional.of(inventoryDeductionResult));

            InventoryDeductionResult previousResult = inventoryCommandService.deductInventory(inventoryDeductionCommand);

            assertThat(inventoryDeductionResult).isEqualTo(previousResult);

            verify(idempotencyPort, never()).acquire(any(), any());
            verify(inventoryCommandRepository, never()).findByProductAndHubIdWithLock(any(), any());
            verify(inventoryCommandRepository, never()).save(any());
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
                    orderId,
                    productId,
                    hubId,
                    30
            );

            given(idempotencyPort.acquire(
                    eq("inventory:restore:" + orderId),
                    any(Duration.class)
            )).willReturn(true);
            given(inventoryCommandRepository.findByProductAndHubIdWithLock(
                    productId,
                    hubId
            )).willReturn(Optional.of(inventory));
            given(inventoryCommandRepository.save(inventory)).willReturn(inventory);

            InventoryRestorationResult inventoryRestorationResultDto = inventoryCommandService.restoreInventory(inventoryRestorationCommand);

            assertThat(inventoryRestorationResultDto.stock()).isEqualTo(130);
            assertThat(inventory.getStock()).isEqualTo(130);

            verify(inventoryCommandRepository).findByProductAndHubIdWithLock(
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
                            orderId,
                            productId,
                            hubId,
                            30
                    );

            given(idempotencyPort.acquire(
                    eq("inventory:restore:" + orderId),
                    any(Duration.class)
            )).willReturn(true);
            given(inventoryCommandRepository.findByProductAndHubIdWithLock(productId, hubId)).willReturn(Optional.empty());

            assertThatThrownBy(
                    () -> inventoryCommandService.restoreInventory(command)
            ).isInstanceOfSatisfying(
                    CustomException.class,
                    exception -> assertThat(exception.getErrorCode())
                            .isEqualTo(InventoryErrorCode.INVENTORY_NOT_FOUND)
            );

            verify(inventoryCommandRepository, never()).save(any(Inventory.class));
        }

        @Test
        @DisplayName("동일 주문의 재고 복원 요청 예외")
        void inventory_restore_duplicate_fail() {
            InventoryRestorationCommand command = new InventoryRestorationCommand(
                    orderId,
                    productId,
                    hubId,
                    30
            );

            given(idempotencyPort.acquire(
                    eq("inventory:restore:" + orderId),
                    any(Duration.class)
            )).willReturn(false);

            assertThatThrownBy(() -> inventoryCommandService.restoreInventory(command))
                    .isInstanceOfSatisfying(CustomException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(InventoryErrorCode.INVENTORY_ALREADY_PROCESSED));

            verify(inventoryCommandRepository, never()).findByProductAndHubIdWithLock(any(), any());
            verify(inventoryCommandRepository, never()).save(any());
        }

        @Test
        @DisplayName("동일 주문의 재고 복원 요청이면 이전 결과 반환")
        void inventory_restore_duplicate_return_result() {
            InventoryRestorationCommand inventoryRestorationCommand = new InventoryRestorationCommand(
                    orderId,
                    productId,
                    hubId,
                    30
            );
            InventoryRestorationResult previousResult = new InventoryRestorationResult(
                    inventoryId,
                    productId,
                    130
            );

            given(idempotencyPort.getResult(
                    eq("inventory:restore:" + orderId),
                    eq(InventoryRestorationResult.class)
            )).willReturn(Optional.of(previousResult));

            InventoryRestorationResult inventoryRestorationResult = inventoryCommandService.restoreInventory(inventoryRestorationCommand);

            assertThat(inventoryRestorationResult).isEqualTo(previousResult);

            verify(idempotencyPort, never()).acquire(any(), any());
            verify(inventoryCommandRepository, never()).findByProductAndHubIdWithLock(any(), any());
            verify(inventoryCommandRepository, never()).save(any());
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

        @Test
        @DisplayName("존재하지 않는 재고 수정 시 예외")
        void inventory_update_not_found() {
            InventoryUpdateCommand command = new InventoryUpdateCommand(
                    inventoryId,
                    70
            );
            given(inventoryCommandRepository.findByIdAndDeletedAtIsNull(inventoryId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryCommandService.updateInventory(command))
                    .isInstanceOfSatisfying(
                            CustomException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(InventoryErrorCode.INVENTORY_NOT_FOUND));

            verify(inventoryCommandRepository).findByIdAndDeletedAtIsNull(inventoryId);
        }
    }

    @Nested
    @DisplayName("재고 삭제")
    class delete {
        @Test
        @DisplayName("재고 삭세 성공")
        void inventory_delete_success() {
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

        @Test
        @DisplayName("존재하지 않는 재고 삭제 시 예외")
        void inventory_delete_not_found() {
            InventoryDeleteCommand inventoryDeleteCommand = new InventoryDeleteCommand(inventoryId);

            given(inventoryCommandRepository.findByIdAndDeletedAtIsNull(inventoryId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryCommandService.deleteInventory(inventoryDeleteCommand, 1L))
                    .isInstanceOfSatisfying(
                            CustomException.class,
                            exception -> assertThat(exception.getErrorCode())
                                    .isEqualTo(InventoryErrorCode.INVENTORY_NOT_FOUND));

            verify(inventoryCommandRepository).findByIdAndDeletedAtIsNull(inventoryId);
        }
    }
}