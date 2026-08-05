package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.command.InventoryCreateCommand;
import com.logistics.inventory.application.dto.command.InventoryDeductionCommand;
import com.logistics.inventory.application.dto.command.InventoryRestorationCommand;
import com.logistics.inventory.application.dto.command.InventoryUpdateCommand;
import com.logistics.inventory.application.dto.result.InventoryCreateResult;
import com.logistics.inventory.application.dto.result.InventoryDeductionResult;
import com.logistics.inventory.application.dto.result.InventoryRestorationResult;
import com.logistics.inventory.application.dto.result.InventoryUpdateResult;
import com.logistics.inventory.application.port.EventPublisher;
import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.domain.repository.InventoryCommandRepository;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.global.exception.InventoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryCommandService {
    private final InventoryCommandRepository inventoryCommandRepository;
    private final EventPublisher eventPublisher;

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
            UUID inventoryId,
            String deletedBy
    ) {

    }

    public InventoryDeductionResult deductInventory(
            InventoryDeductionCommand inventoryDeductionCommand
    ) {
        // 상품 아이디와 허브 아이디로 재고 조회, 일단 임시로 만들 예정
        // 재고 없으면 예외
        Inventory inventory = inventoryCommandRepository.findByProductAndHubId(
                inventoryDeductionCommand.productId(),
                inventoryDeductionCommand.hubId()
        ).orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        // 재고 도메인에 차감 요청
        inventory.deduct(inventoryDeductionCommand.stock());
        // 변경된 재고 저장
        Inventory savedInventory = inventoryCommandRepository.save(inventory);
        // 차감 후 결과 DTO 반환
        return InventoryDeductionResult.from(savedInventory);
    }

    public InventoryRestorationResult restoreInventory(
            InventoryRestorationCommand inventoryRestorationCommand
    ) {
        Inventory inventory = inventoryCommandRepository.findByProductAndHubId(
                inventoryRestorationCommand.productId(),
                inventoryRestorationCommand.hubId()
        ).orElseThrow(() -> new CustomException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        inventory.restore(inventoryRestorationCommand.stock());

        Inventory savedInventory = inventoryCommandRepository.save(inventory);

        return InventoryRestorationResult.from(savedInventory);
    }
}
