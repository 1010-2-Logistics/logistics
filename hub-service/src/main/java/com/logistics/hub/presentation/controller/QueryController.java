package com.logistics.hub.presentation.controller;

import com.logistics.hub.application.dto.query.GetHubQuery;
import com.logistics.hub.application.dto.query.SearchHubQuery;
import com.logistics.hub.application.service.HubQueryService;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.global.response.ApiResponse;
import com.logistics.hub.global.response.PageResponse;
import com.logistics.hub.presentation.controller.dto.response.HubResponseDto;
import com.logistics.hub.presentation.controller.dto.response.HubSummaryResponseDto;
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
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class QueryController {

    private final HubQueryService hubQueryService;

    @GetMapping("/{hubId}")
    public ApiResponse<HubResponseDto> get(@PathVariable UUID hubId) {
        Hub hub = hubQueryService.get(new GetHubQuery(hubId));
        return ApiResponse.success(200, "샘플 조회 성공", HubResponseDto.from(hub));
    }

    @GetMapping
    public ApiResponse<PageResponse<HubSummaryResponseDto>> search(
            @RequestParam(required = false) UUID hubId,
            Pageable pageable) {
        Page<Hub> page = hubQueryService.search(new SearchHubQuery(hubId, pageable));
        Page<HubSummaryResponseDto> responsePage = page.map(HubSummaryResponseDto::from);
        return ApiResponse.success(200, "샘플 목록 조회 성공", PageResponse.of(responsePage));
    }
}
