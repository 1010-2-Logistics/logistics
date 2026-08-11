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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name= "HubRoute")
@RestController
@RequestMapping("/api/v1/hubRoute")
@RequiredArgsConstructor
public class HubRouteQueryController {

    private final HubRouteQueryService hubRouteQueryService;

    @Operation(
            summary = "허브 경로 상세 조회",
            description = """
                     접근 권한:
                    - 모든 로그인 사용자
                    """
    )
    @GetMapping("/{hubRouteId}")
    public ApiResponse<HubRouteResponseDto> get(@PathVariable UUID hubRouteId) {
        HubRouteResponseDto hubRouteResponseDto = hubRouteQueryService.getCachedDto(new GetHubRouteQuery(hubRouteId));
        return ApiResponse.success(200, "허브 경로 조회 성공", hubRouteResponseDto);
    }

    @Operation(
            summary = "허브 경로 목록 조회",
            description = """
                     접근 권한:
                    - 모든 로그인 사용자
                    """
    )
    @GetMapping
    public ApiResponse<PageResponse<HubRouteSummaryResponseDto>> search(
            @RequestParam(required = false) UUID hubRouteId,
            Pageable pageable) {
        PageResponse<HubRouteSummaryResponseDto> response = hubRouteQueryService.search(new SearchHubRouteQuery(hubRouteId, pageable));

        return ApiResponse.success(200, "허브 경로 목록 조회 성공", response);
    }

    @Operation(
            summary = "허브 경로 탐색 -> 연결되어 있지 않은 허브 경로 탐색",
            description = """
                     접근 권한:
                    - 모든 로그인 사용자
                    """
    )
    //허브 경로 탐색 (시작허브와 도착 허브를 받아서 검색 -> 연결되어 있지 않은 허브 경로 검색)
    @GetMapping("/findHubRoute")
    public ApiResponse<HubRouteFindResponseDto> findHubRoute(@ModelAttribute HubRouteFindRequestDto requestDto){
        //redis를 통해 한번 찾거나 조합한 경로는 redis에 저장된다.
        HubRouteFindResponseDto hubRouteFindResponseDto = hubRouteQueryService.findHubRoute(requestDto);

        return ApiResponse.success(200,"허브 경로 조회 성공",hubRouteFindResponseDto);
    }
}
