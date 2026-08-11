package com.logistics.hub.presentation.controller;

import com.logistics.hub.application.dto.query.GetHubQuery;
import com.logistics.hub.application.dto.query.SearchHubQuery;
import com.logistics.hub.application.service.HubQueryService;
import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.global.response.ApiResponse;
import com.logistics.hub.global.response.PageResponse;
import com.logistics.hub.infrastructure.security.principal.UserPrincipal;
import com.logistics.hub.presentation.dto.dto.response.HubResponseDto;
import com.logistics.hub.presentation.dto.dto.response.HubSummaryResponseDto;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name= "Hub")
@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class QueryController {

    private final HubQueryService hubQueryService;

    @Operation(
            summary = "허브 상세 조회",
            description = """
                     접근 권한:
                    - 모든 로그인 사용자
                    """
    )
    @GetMapping("/{hubId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<HubResponseDto> get(@PathVariable UUID hubId) {
        Hub hub = hubQueryService.get(new GetHubQuery(hubId));
        return ApiResponse.success(200, "허브 조회 성공", HubResponseDto.from(hub));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "허브 목록 조회",
            description = """
                     접근 권한:
                    - 모든 로그인 사용자
                    """
    )
    @GetMapping
    public ApiResponse<PageResponse<HubSummaryResponseDto>> search(
            @RequestParam(required = false) UUID hubId,
            Pageable pageable) {
        Page<Hub> page = hubQueryService.search(new SearchHubQuery(hubId, pageable));
        Page<HubSummaryResponseDto> responsePage = page.map(HubSummaryResponseDto::from);
        return ApiResponse.success(200, "허브 목록 조회 성공", PageResponse.of(responsePage));
    }
}
