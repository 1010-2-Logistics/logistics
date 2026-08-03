package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.command.InventoryCreateCommand;
import com.logistics.inventory.application.dto.command.InventoryUpdateCommand;
import com.logistics.inventory.application.port.EventPublisher;
import com.logistics.inventory.domain.repository.InventoryCommandRepository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
