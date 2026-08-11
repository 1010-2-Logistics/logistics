package com.logistics.hub.application.service;

import com.logistics.hub.application.dto.query.GetHubQuery;
import com.logistics.hub.application.dto.query.SearchHubQuery;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubQueryRepository;
import com.logistics.hub.global.exception.CustomException;
import com.logistics.hub.global.exception.HubErrorCode;
import com.logistics.hub.infrastructure.security.principal.UserPrincipal;
import com.logistics.hub.presentation.dto.dto.response.HubResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubQueryService {

    private final HubQueryRepository hubQueryRepository;

    public Hub get(GetHubQuery query) {
        return hubQueryRepository.findByIdAndDeletedAtIsNull(query.hubId())
                .orElseThrow(() -> new CustomException(HubErrorCode.HUB_NOT_FOUND));
    }

    public Page<Hub> search(SearchHubQuery query) {
        return hubQueryRepository.search(query.hubId(), query.pageable());
    }

    public Set<UUID> findValidHubIdsIn(List<UUID> hubIds) {
        return hubQueryRepository.findValidHubIdsIn(hubIds);
    }


    public Set<HubResponseDto> getHubsInternal(List<UUID> hubIds) {
        if (hubIds == null || hubIds.isEmpty()) {
            throw new CustomException(HubErrorCode.HUB_NOT_FOUND);
        }

        List<Hub> hubs = hubQueryRepository.findAllByHubIdInAndDeletedAtIsNull(hubIds);

        // 존재 하지 않는 허브ID가 있는지 확인 -> Id개수와 찾은 정보 개수 일치 하는지 확인
        Set<UUID> requestedUniqueIds = new HashSet<>(hubIds);
        if (hubs.size() != requestedUniqueIds.size()) {
            throw new CustomException(HubErrorCode.HUB_NOT_FOUND);
        }

        return hubs.stream()
                .map(HubResponseDto::from)
                .collect(Collectors.toSet());
    }
}
