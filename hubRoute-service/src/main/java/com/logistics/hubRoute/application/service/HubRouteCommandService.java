package com.logistics.hubRoute.application.service;

import com.logistics.hubRoute.application.dto.command.HubRouteCreateCommand;
import com.logistics.hubRoute.application.port.EventPublisher;
import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.repository.HubRouteCommandRepository;
import com.logistics.hubRoute.global.exception.CustomException;
import com.logistics.hubRoute.global.exception.HubRouteErrorCode;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.logistics.hubRoute.infrastructure.feign.client.HubClient;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteCreateResponseDto;
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
    private final HubClient hubClient;

    //허브 경로 등록
    public HubRouteCreateResponseDto createHubRoute(@Valid HubRouteCreateCommand hubRouteCreateCommand) {
        UUID startHubId = hubRouteCreateCommand.startHubId();
        UUID endHubId = hubRouteCreateCommand.endHubId();

        //도착허브와 출발허브가 동일한지 체크
        if (startHubId.equals(endHubId)) {
            throw new CustomException(HubRouteErrorCode.HUB_START_END_SAME);
        }

        //출발허브 도착허브 존재하는지 체크
        List<UUID> targetHubIds = List.of(startHubId, endHubId);
        Set<UUID> existingHubIds = hubClient.validateHubIds(targetHubIds);

        if (!existingHubIds.contains(startHubId)) {
            throw new CustomException(HubRouteErrorCode.HUB_NOT_FOUND);
        }

        if (!existingHubIds.contains(endHubId)) {
            throw new CustomException(HubRouteErrorCode.HUB_NOT_FOUND);
        }

        //중복 경로 탐색
        if (hubRouteCommandRepository.existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(startHubId, endHubId)) {
            throw new CustomException(HubRouteErrorCode.HUB_ROUTE_ALREADY_EXISTS);
        }

        HubRoute hubRoute = HubRoute.create(
                startHubId,
                endHubId,
                hubRouteCreateCommand.duration(),
                hubRouteCreateCommand.distance(),
                hubRouteCreateCommand.createdBy()
        );

        hubRouteCommandRepository.save(hubRoute);

        return new HubRouteCreateResponseDto(hubRoute.getHubRouteId());
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
