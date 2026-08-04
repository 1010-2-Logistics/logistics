package com.logistics.hub.application.service;

import com.logistics.hub.application.dto.query.GetHubQuery;
import com.logistics.hub.application.dto.query.SearchHubQuery;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubQueryRepository;
import com.logistics.hub.global.exception.CustomException;
import com.logistics.hub.global.exception.HubErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
