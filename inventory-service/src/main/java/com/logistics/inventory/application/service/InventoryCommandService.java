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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryCommandService {
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
            InventoryUpdateCommand command
    ) {
        Inventory inventory = inventoryCommandRepository
                .findByIdAndDeletedAtIsNull(command.inventoryId())
                .orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        inventory.updateStock(command.stock());

        Inventory savedInventory = inventoryCommandRepository.save(inventory);

        return InventoryUpdateResult.from(savedInventory);
    }

    public void deleteInventory(
            InventoryDeleteCommand inventoryDeleteCommand,
            Long deletedBy
    ) {
        Inventory inventory = inventoryCommandRepository.findByIdAndDeletedAtIsNull(inventoryDeleteCommand.inventoryId())
                .orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        inventory.delete(deletedBy);

        inventoryCommandRepository.save(inventory);
    }

    public InventoryDeductionResult deductInventory(
            InventoryDeductionCommand inventoryDeductionCommand
    ) {
        String key = "inventory:deduct:" + inventoryDeductionCommand.orderId();

        boolean acquired = idempotencyPort.acquire(
                key,
                Duration.ofMinutes(10)
        );
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

            return InventoryDeductionResult.from(savedInventory);

        } catch (RuntimeException e) {
            idempotencyPort.release(key);

            throw e;
        }
    }

    public InventoryRestorationResult restoreInventory(
            InventoryRestorationCommand inventoryRestorationCommand
    ) {
        String key = "inventory:restore:" + inventoryRestorationCommand.orderId();

        boolean acquired = idempotencyPort.acquire(
                key,
                Duration.ofMinutes(10)
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

            return InventoryRestorationResult.from(savedInventory);

        } catch (RuntimeException e) {
            idempotencyPort.release(key);

            throw e;
        }
    }
}
