package com.logistics.hubRoute.application.service;

import com.logistics.hubRoute.application.dto.command.HubRouteCreateCommand;
import com.logistics.hubRoute.application.dto.command.HubRouteUpdateCommand;
import com.logistics.hubRoute.application.port.EventPublisher;
import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.repository.HubRouteCommandRepository;
import com.logistics.hubRoute.global.exception.CustomException;
import com.logistics.hubRoute.global.exception.HubRouteErrorCode;
import java.util.UUID;

import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteCreateResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HubRouteCommandService {

    private final HubRouteCommandRepository hubRouteCommandRepository;
    private final EventPublisher eventPublisher;

    //허브 등록
    public HubRouteCreateResponseDto createHubRoute(@Valid HubRouteCreateCommand hubRouteCreateCommand) {
        return null;
    }

//    //허브 수정
//    public HubRouteResponseDto updateHub(UUID hubId, HubRouteUpdateCommand hubRouteUpdateCommand) {
//        return null;
//    }
//
//    //허브 삭제
//    public void deleteHub(UUID hubId, long deletedBy) {

   // }
}
