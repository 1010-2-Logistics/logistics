package com.logistics.template.application.service;

import com.logistics.template.application.dto.command.CreateInventoryCommand;
import com.logistics.template.application.dto.command.UpdateInventoryCommand;
import com.logistics.template.application.event.InventoryCreatedEvent;
import com.logistics.template.application.port.EventPublisher;
import com.logistics.template.domain.entity.Inventory;
import com.logistics.template.domain.repository.InventoryCommandRepository;
import com.logistics.template.global.exception.CustomException;
import com.logistics.template.global.exception.InventoryErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryCommandService {

    private final InventoryCommandRepository sampleCommandRepository;
    private final EventPublisher eventPublisher;

    public UUID create(CreateInventoryCommand command) {
        Inventory sample = Inventory.create(command.name());
        sampleCommandRepository.save(sample);
        eventPublisher.publish(new InventoryCreatedEvent(sample.getInventoryId(), sample.getName()));
        return sample.getInventoryId();
    }

    public void update(UpdateInventoryCommand command) {
        Inventory sample = sampleCommandRepository.findByIdAndDeletedAtIsNull(command.sampleId())
                .orElseThrow(() -> new CustomException(InventoryErrorCode.SAMPLE_NOT_FOUND));
        sample.update(command.name());
    }

    public void delete(UUID sampleId, String deletedBy) {
        Inventory sample = sampleCommandRepository.findByIdAndDeletedAtIsNull(sampleId)
                .orElseThrow(() -> new CustomException(InventoryErrorCode.SAMPLE_NOT_FOUND));
        sample.markDeleted(deletedBy);
    }
}
