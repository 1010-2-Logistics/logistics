package com.logistics.hub.application.service;

import com.logistics.hub.application.dto.command.CreateHubCommand;
import com.logistics.hub.application.dto.command.UpdateHubCommand;
import com.logistics.hub.application.event.HubCreatedEvent;
import com.logistics.hub.application.port.EventPublisher;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubCommandRepository;
import com.logistics.hub.global.exception.CustomException;
import com.logistics.hub.global.exception.HubErrorCode;
import java.util.UUID;

import com.logistics.hub.presentation.controller.dto.request.HubCreateRequestDto;
import com.logistics.hub.presentation.controller.dto.response.HubCreateResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HubCommandService {

    private final HubCommandRepository hubCommandRepository;
    private final EventPublisher eventPublisher;

    public HubCreateResponseDto createHub(@Valid HubCreateRequestDto request) {

        Hub hub = Hub.create(
                request.getHubName(),
                request.getHubAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getCreatedBy()
        );

        hubCommandRepository.save(hub);
        return new HubCreateResponseDto(hub.getHubId());
    }

    public void update(UpdateHubCommand command) {
        Hub hub = hubCommandRepository.findByIdAndDeletedAtIsNull(command.sampleId())
                .orElseThrow(() -> new CustomException(HubErrorCode.SAMPLE_NOT_FOUND));
        hub.update(command.name());
    }

    public void delete(UUID sampleId, Long deletedBy) {
        Hub hub = hubCommandRepository.findByIdAndDeletedAtIsNull(sampleId)
                .orElseThrow(() -> new CustomException(HubErrorCode.SAMPLE_NOT_FOUND));
        hub.markDeleted(deletedBy);
    }


}
