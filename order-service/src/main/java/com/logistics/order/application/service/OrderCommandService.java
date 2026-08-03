package com.logistics.order.application.service;


import java.util.UUID;

import com.logistics.order.application.dto.command.CreateSampleCommand;
import com.logistics.order.application.dto.command.UpdateSampleCommand;
import com.logistics.order.application.port.EventPublisher;
import com.logistics.order.domain.repository.OrderCommandRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private final OrderCommandRepository sampleCommandRepository;
    private final EventPublisher eventPublisher;

    public UUID create(CreateSampleCommand command) {
       return null;
    }

    public void update(UpdateSampleCommand command) {

    }

    public void delete(UUID sampleId, String deletedBy) {

    }
}
