package com.logistics.hubRoute.presentation.controller;

import com.logistics.hubRoute.application.dto.query.GetHubRouteQuery;
import com.logistics.hubRoute.application.dto.query.SearchHubRouteQuery;
import com.logistics.hubRoute.application.service.HubRouteQueryService;
import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.global.response.ApiResponse;
import com.logistics.hubRoute.global.response.PageResponse;
import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteFindRequestDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteFindResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteResponse;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteSummaryResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hubRoute")
@RequiredArgsConstructor
public class HubRouteQueryController {

    private final HubRouteQueryService hubRouteQueryService;

    @GetMapping("/{hubRouteId}")
    public ApiResponse<HubRouteResponseDto> get(@PathVariable UUID hubRouteId) {
        HubRouteResponseDto hubRouteResponseDto = hubRouteQueryService.getCachedDto(new GetHubRouteQuery(hubRouteId));
        return ApiResponse.success(200, "허브 경로 조회 성공", hubRouteResponseDto);
    }

    @GetMapping
    public ApiResponse<PageResponse<HubRouteSummaryResponseDto>> search(
            @RequestParam(required = false) UUID hubRouteId,
            Pageable pageable) {

        // 기존 Query 객체 생성 방식 그대로 유지
        PageResponse<HubRouteSummaryResponseDto> response = hubRouteQueryService.search(new SearchHubRouteQuery(hubRouteId, pageable));

        return ApiResponse.success(200, "허브 경로 목록 조회 성공", response);
    }

    //허브 경로 탐색 (시작허브와 도착 허브를 받아서 검색 -> 연결되어 있지 않은 허브 경로 검색)
    @GetMapping("/findHubRoute")
    public ApiResponse<HubRouteFindResponseDto> findHubRoute(@RequestBody HubRouteFindRequestDto requestDto){
        //redis를 통해 한번 찾거나 조합한 경로는 redis에 저장된다.
        HubRouteFindResponseDto hubRouteFindResponseDto = hubRouteQueryService.findHubRoute(requestDto);

        return ApiResponse.success(200,"허브 경로 조회 성공",hubRouteFindResponseDto);
    }


}
