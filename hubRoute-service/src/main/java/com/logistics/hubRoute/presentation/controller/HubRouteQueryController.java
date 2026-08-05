package com.logistics.hubRoute.presentation.controller;

import com.logistics.hubRoute.application.dto.query.GetHubRouteQuery;
import com.logistics.hubRoute.application.dto.query.SearchHubRouteQuery;
import com.logistics.hubRoute.application.service.HubRouteQueryService;
import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.global.response.ApiResponse;
import com.logistics.hubRoute.global.response.PageResponse;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteResponse;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteSummaryResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hubRoute")
@RequiredArgsConstructor
public class HubRouteQueryController {

    private final HubRouteQueryService hubRouteQueryService;

    @GetMapping("/{hubRouteId}")
    public ApiResponse<HubRouteResponseDto> get(@PathVariable UUID hubRouteId) {
        HubRoute hub = hubRouteQueryService.get(new GetHubRouteQuery(hubRouteId));
        return ApiResponse.success(200, "허브 경로 조회 성공", HubRouteResponseDto.from(hub));
    }

    @GetMapping
    public ApiResponse<PageResponse<HubRouteSummaryResponseDto>> search(
            @RequestParam(required = false) UUID hubRouteId,
            Pageable pageable) {
        Page<HubRoute> page = hubRouteQueryService.search(new SearchHubRouteQuery(hubRouteId, pageable));
        Page<HubRouteSummaryResponseDto> responsePage = page.map(HubRouteSummaryResponseDto::from);
        return ApiResponse.success(200, "허브 경로 목록 조회 성공", PageResponse.of(responsePage));
    }
}
