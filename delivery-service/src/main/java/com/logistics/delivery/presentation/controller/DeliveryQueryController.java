package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.dto.query.SearchDeliveryQuery;
import com.logistics.delivery.application.service.DeliveryQueryService;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryStatus;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.global.response.PageResponse;
import com.logistics.delivery.presentation.controller.dto.response.DeliveryResponse;
import com.logistics.delivery.presentation.controller.dto.response.DeliverySummaryResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryQueryController {

    private final DeliveryQueryService deliveryQueryService;

    @GetMapping
    public ApiResponse<PageResponse<DeliverySummaryResponse>> search(
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        SearchDeliveryQuery query = SearchDeliveryQuery.of(status, hubId, sort, page, size);
        Page<DeliverySummaryResponse> result = deliveryQueryService.search(query).map(DeliverySummaryResponse::from);
        return ApiResponse.success(200, "배송 목록 조회 성공", PageResponse.of(result));
    }

    @GetMapping("/{deliveryId}")
    public ApiResponse<DeliveryResponse> getById(@PathVariable UUID deliveryId) {
        var result = deliveryQueryService.getById(deliveryId);
        return ApiResponse.success(200, "배송 조회 성공", DeliveryResponse.from(result));
    }
}