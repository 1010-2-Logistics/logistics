package com.logistics.hubRoute.application.service;

import com.logistics.hubRoute.application.dto.query.GetHubRouteQuery;
import com.logistics.hubRoute.application.dto.query.SearchHubRouteQuery;
import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.repository.HubRouteQueryRepository;
import com.logistics.hubRoute.global.exception.CustomException;
import com.logistics.hubRoute.global.exception.HubRouteErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubRouteQueryService {

    private final HubRouteQueryRepository hubRouteQueryRepository;

    public HubRoute get(GetHubRouteQuery query) {
        return hubRouteQueryRepository.findByIdAndDeletedAtIsNull(query.hubRouteId())
                .orElseThrow(() -> new CustomException(HubRouteErrorCode.HUB_NOT_FOUND));
    }

    public Page<HubRoute> search(SearchHubRouteQuery query) {
        return hubRouteQueryRepository.search(query.hubRouteId(), query.pageable());
    }
}
