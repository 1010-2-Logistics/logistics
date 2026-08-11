package com.logistics.inventory.application.service;

import com.logistics.inventory.application.authorization.InventoryAuthorizationService;
import com.logistics.inventory.application.dto.auth.AuthenticatedUser;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryCommandService {
    private final InventoryAuthorizationService inventoryAuthorizationService;
    private final InventoryCommandRepository inventoryCommandRepository;
    private final EventPublisher eventPublisher;
    private final IdempotencyPort idempotencyPort;

    public InventoryCreateResult createInventory(
            InventoryCreateCommand createInventoryCommand
    ) {
        Optional<Inventory> existingInventory = inventoryCommandRepository
                .findByProductIdAndHubIdAndDeletedAtIsNull(
                        createInventoryCommand.productId(),
                        createInventoryCommand.hubId()
                );

        if (existingInventory.isPresent()) {
            throw new CustomException(InventoryErrorCode.INVENTORY_ALREADY_EXISTS);
        }

        Inventory inventory = Inventory.create(
                createInventoryCommand.productId(),
                createInventoryCommand.hubId(),
                createInventoryCommand.stock()
        );

        Inventory savedInventory = inventoryCommandRepository.save(inventory);

        return new InventoryCreateResult(savedInventory.getInventoryId());
    }

    public InventoryUpdateResult updateInventory(
            InventoryUpdateCommand inventoryUpdateCommand,
            AuthenticatedUser authenticatedUser
    ) {
        Inventory inventory = inventoryCommandRepository
                .findByIdAndDeletedAtIsNull(inventoryUpdateCommand.inventoryId())
                .orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        inventoryAuthorizationService.validateHubAccess(
                authenticatedUser,
                inventory.getHubId()
        );

        inventory.updateStock(inventoryUpdateCommand.stock());

        Inventory savedInventory = inventoryCommandRepository.save(inventory);

        return InventoryUpdateResult.from(savedInventory);
    }

    public void deleteInventory(
            InventoryDeleteCommand inventoryDeleteCommand,
            AuthenticatedUser authenticatedUser
    ) {
        Inventory inventory = inventoryCommandRepository
                .findById(inventoryDeleteCommand.inventoryId())
                .orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        if (inventory.getDeletedAt() != null) {
            throw new CustomException(
                    InventoryErrorCode.INVENTORY_DELETE_CONFLICT
            );
        }

        inventoryAuthorizationService.validateHubAccess(
                authenticatedUser,
                inventory.getHubId()
        );

        inventory.delete(authenticatedUser.userId());
    }

    public InventoryDeductionResult deductInventory(
            InventoryDeductionCommand inventoryDeductionCommand
    ) {
        String key = "inventory:deduct:" + inventoryDeductionCommand.orderId();
        Duration ttl = Duration.ofMinutes(10);

        // 핵심 코드 : 이 주문 예전에 성공한 적 있어? 있으면 그때 결과 다시 줘
        Optional<InventoryDeductionResult> previousResult = idempotencyPort.getResult(
                key,
                InventoryDeductionResult.class
        );
        if (previousResult.isPresent()) {
            return previousResult.get();
        }

        // 내가 이 요청 처리해도 되는 첫 번째임 ? yes : no
        boolean acquired = idempotencyPort.acquire(key, ttl);

        if (!acquired) {
            throw new CustomException(InventoryErrorCode.INVENTORY_ALREADY_PROCESSED);
        }

        try {
            Inventory inventory = inventoryCommandRepository.findByProductAndHubIdWithLock(
                    inventoryDeductionCommand.productId(),
                    inventoryDeductionCommand.hubId()
            ).orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

            inventory.deduct(inventoryDeductionCommand.quantity());
            Inventory savedInventory = inventoryCommandRepository.save(inventory);
            InventoryDeductionResult result = InventoryDeductionResult.from(savedInventory);

            // 이 주문 처리 성공했으니까 결과를 Redis에 저장해둬
            idempotencyPort.complete(key, result, ttl);

            return result;
        } catch (RuntimeException e) {
            idempotencyPort.release(key);

            throw e;
        }
    }

    public InventoryRestorationResult restoreInventory(
            InventoryRestorationCommand inventoryRestorationCommand
    ) {
        String key = "inventory:restore:" + inventoryRestorationCommand.orderId();
        Duration ttl = Duration.ofMinutes(10);

        Optional<InventoryRestorationResult> previousResult = idempotencyPort.getResult(
                key,
                InventoryRestorationResult.class
        );

        if (previousResult.isPresent()) {
            return previousResult.get();
        }

        boolean acquired = idempotencyPort.acquire(
                key,
                ttl
        );
        if (!acquired) {
            throw new CustomException(InventoryErrorCode.INVENTORY_ALREADY_PROCESSED);
        }

        try {
            Inventory inventory = inventoryCommandRepository.findByProductAndHubIdWithLock(
                    inventoryRestorationCommand.productId(),
                    inventoryRestorationCommand.hubId()
            ).orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

            inventory.restore(inventoryRestorationCommand.quantity());
            Inventory savedInventory = inventoryCommandRepository.save(inventory);

            InventoryRestorationResult result = InventoryRestorationResult.from(savedInventory);

            idempotencyPort.complete(key, result, ttl);

            return result;

        } catch (RuntimeException e) {
            idempotencyPort.release(key);

            throw e;
        }
    }
}
