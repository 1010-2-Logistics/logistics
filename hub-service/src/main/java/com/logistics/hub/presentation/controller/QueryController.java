package com.logistics.hub.presentation.controller;

import com.logistics.hub.application.service.HubQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class QueryController {

    private final HubQueryService hubQueryService;

//    @GetMapping("/{hubId}")
//    public ApiResponse<HubResponse> get(@PathVariable UUID hubId) {
//        Hub hub = hubQueryService.get(new GetHubQuery(hubId));
//        return ApiResponse.success(200, "샘플 조회 성공", HubResponse.from(hub));
//    }
//
//    @GetMapping
//    public ApiResponse<PageResponse<HubSummaryResponse>> search(
//            @RequestParam(required = false) UUID hubId,
//            Pageable pageable) {
//        Page<Hub> page = hubQueryService.search(new SearchHubQuery(hubId, pageable));
//        Page<HubSummaryResponse> responsePage = page.map(HubSummaryResponse::from);
//        return ApiResponse.success(200, "샘플 목록 조회 성공", PageResponse.of(responsePage));
//    }
}
