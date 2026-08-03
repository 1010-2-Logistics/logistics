package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.command.InventoryCreateCommand;
import com.logistics.inventory.application.dto.command.InventoryDeductionCommand;
import com.logistics.inventory.application.dto.command.InventoryRestorationCommand;
import com.logistics.inventory.application.dto.command.InventoryUpdateCommand;
import com.logistics.inventory.application.dto.result.InventoryDeductionResultDto;
import com.logistics.inventory.application.dto.result.InventoryRestorationResultDto;
import com.logistics.inventory.application.port.EventPublisher;
import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.domain.repository.InventoryCommandRepository;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.global.exception.InventoryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryCommandService {
    private final InventoryCommandRepository inventoryCommandRepository;
    private final EventPublisher eventPublisher;

    public UUID createInventory(InventoryCreateCommand createInventoryCommand) {
        return null;
    }

    public void updateInventory(InventoryUpdateCommand updateInventoryCommand) {
    }

    public void deleteInventory(
            UUID inventoryId,
            String deletedBy
    ) {

    }

    public InventoryDeductionResultDto deductInventory(
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
        return InventoryDeductionResultDto.from(savedInventory);
    }

    public InventoryRestorationResultDto restoreInventory(
            InventoryRestorationCommand inventoryRestorationCommand
    ) {
        return null;
    }
}
