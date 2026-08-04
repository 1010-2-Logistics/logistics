package com.logistics.hub.application.service;

import com.logistics.hub.application.dto.command.HubCreateCommand;
import com.logistics.hub.application.dto.command.HubUpdateCommand;
import com.logistics.hub.application.port.EventPublisher;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubCommandRepository;
import com.logistics.hub.global.exception.CustomException;
import com.logistics.hub.global.exception.HubErrorCode;
import java.util.UUID;

import com.logistics.hub.presentation.dto.dto.response.HubCreateResponseDto;
import com.logistics.hub.presentation.dto.dto.response.HubResponseDto;
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

    public HubCreateResponseDto createHub(@Valid HubCreateCommand hubCreateCommand) {


        //위도 경도 중복 검사
        if(hubCommandRepository.existsByLatitudeAndLongitudeAndDeletedAtIsNull(hubCreateCommand.latitude(),hubCreateCommand.longitude()))
        {
            throw new CustomException(HubErrorCode.HUB_ALPEADY_EXISTS);
        }

        //허브 주소가 동일한지 검사
        if(hubCommandRepository.existsByHubAddressAndDeletedAtIsNull(hubCreateCommand.hubAddress()))
        {
            throw new CustomException(HubErrorCode.HUB_ALPEADY_EXISTS);
        }

        Hub hub = Hub.create(
                hubCreateCommand.hubName(),
                hubCreateCommand.hubAddress(),
                hubCreateCommand.latitude(),
                hubCreateCommand.longitude(),
                hubCreateCommand.createdBy()//임시 코드임
        );

        hubCommandRepository.save(hub);
        return new HubCreateResponseDto(hub.getHubId());
    }

    public HubResponseDto updateHub(UUID hubId, HubUpdateCommand hubUpdateCommand) {

        //허브가 존재하는지 체크
        if(!hubCommandRepository.findByhubIdAndDeletedAtIsNull(hubId))
        {
            throw new CustomException(HubErrorCode.HUB_NOT_FOUND);
        }

        //자신의 허브아이디와 다르면서 위도와 경도가 동일한 허브가 있는지 체크
        if(hubCommandRepository.existsByLatitudeAndLongitudeAndHubIdNotAndDeletedAtIsNull(
                hubUpdateCommand.latitude(),
                hubUpdateCommand.longitude(),
                hubId))
        {
            throw new CustomException(HubErrorCode.HUB_ALPEADY_EXISTS);
        }

        //자신의 허브아이디와 다르면서 허브 주소가 동일한지 검사
        if(hubCommandRepository.existsByHubAddressAndHubIdNotAndDeletedAtIsNull(
                hubUpdateCommand.hubAddress(),
                hubId
        ))
        {
            throw new CustomException(HubErrorCode.HUB_ALPEADY_EXISTS);
        }

        Hub hub = hubCommandRepository.findByIdAndDeletedAtIsNull(hubId).get();
        hub.update(
                hubUpdateCommand.hubName(),
                hubUpdateCommand.hubAddress(),
                hubUpdateCommand.latitude(),
                hubUpdateCommand.longitude()
        );

        return new HubResponseDto(hub.getHubId(), hub.getHubName(), hub.getHubAddress());
    }

    public void delete(UUID hubId, Long deletedBy) {
        Hub hub = hubCommandRepository.findByIdAndDeletedAtIsNull(hubId)
                .orElseThrow(() -> new CustomException(HubErrorCode.HUB_NOT_FOUND));
        hub.markDeleted(deletedBy);
    }



}
