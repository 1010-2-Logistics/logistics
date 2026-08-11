package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.dto.query.SearchDeliveryQuery;
import com.logistics.delivery.application.service.DeliveryQueryService;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.global.response.PageResponse;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import com.logistics.delivery.presentation.dto.response.DeliveryResponse;
import com.logistics.delivery.presentation.dto.response.DeliveryRouteListResponse;
import com.logistics.delivery.presentation.dto.response.DeliverySummaryResponse;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name= "Delivery")
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryQueryController {

    private final DeliveryQueryService deliveryQueryService;

    @Operation(
            summary = "배송 목록 조회",
            description = """
                 접근 권한:
                - MASTER: 전체 조회 가능
                - HUB_MANAGER: 담당 허브 관련 배송만 조회 가능
                - HUB_DELIVERY_MANAGER, COMPANY_DELIVERY_MANAGER: 본인 배송건만 조회 가능
                - COMPANY_MANAGER: 전체 조회 가능
                """
    )
    @GetMapping
    public ApiResponse<PageResponse<DeliverySummaryResponse>> searchDelivery(
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal UserPrincipal principal) {
        SearchDeliveryQuery query = SearchDeliveryQuery.of(status, hubId, sort, page, size);
        Page<DeliverySummaryResponse> result = deliveryQueryService.searchDelivery(query, principal).map(DeliverySummaryResponse::from);
        return ApiResponse.success(200, "배송 목록 조회 성공", PageResponse.of(result));
    }

    @Operation(
            summary = "배송 단건 조회",
            description = """
                 접근 권한:
                - MASTER: 전체 조회 가능
                - 담당 허브관리자: 담당 허브 소속 배송만 조회 가능
                - 배송담당자: 본인 배송건만 조회 가능
                - COMPANY_MANAGER: 전체 조회 가능 (본인 주문 여부는 order-service에서 검증)
                """
    )
    @GetMapping("/{deliveryId}")
    public ApiResponse<DeliveryResponse> getDeliveryById(@PathVariable UUID deliveryId,
            @AuthenticationPrincipal UserPrincipal principal) {
        var result = deliveryQueryService.getDeliveryById(deliveryId, principal);
        return ApiResponse.success(200, "배송 조회 성공", DeliveryResponse.from(result));
    }

    @Operation(
            summary = "배송 경로 조회",
            description = """
                 접근 권한:
                - MASTER: 전체 조회 가능
                - HUB_MANAGER: 담당 허브만 조회 가능
                - HUB_DELIVERY_MANAGER, COMPANY_DELIVERY_MANAGER: 본인 배송건만 조회 가능
                - COMPANY_MANAGER: 전체 조회 가능
                """
    )
    @GetMapping("/{deliveryId}/routes")
    public ApiResponse<DeliveryRouteListResponse> getDeliveryRoutes(@PathVariable UUID deliveryId,
            @AuthenticationPrincipal UserPrincipal principal) {
        var result = deliveryQueryService.getDeliveryRoutes(deliveryId, principal);
        return ApiResponse.success(200, "배송 경로 조회 성공", DeliveryRouteListResponse.from(result));
    }
}