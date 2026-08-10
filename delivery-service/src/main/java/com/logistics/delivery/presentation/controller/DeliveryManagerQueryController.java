package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.dto.query.SearchDeliveryManagerQuery;
import com.logistics.delivery.application.service.DeliveryManagerQueryService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.global.response.PageResponse;
import com.logistics.delivery.presentation.dto.response.DeliveryManagerResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerQueryController {

    private final DeliveryManagerQueryService deliveryManagerQueryService;

    @GetMapping
    public ApiResponse<PageResponse<DeliveryManagerResponse>> search(
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) ManagerType managerType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        SearchDeliveryManagerQuery query = SearchDeliveryManagerQuery.of(managerType, hubId, page, size);
        Page<DeliveryManagerResponse> result = deliveryManagerQueryService.search(query).map(DeliveryManagerResponse::from);
        return ApiResponse.success(200, "배송 담당자 목록 조회 성공", PageResponse.of(result));
    }

    @GetMapping("/{deliveryManagerId}")
    public ApiResponse<DeliveryManagerResponse> getById(@PathVariable Long deliveryManagerId) {
        DeliveryManager manager = deliveryManagerQueryService.getById(deliveryManagerId);
        return ApiResponse.success(200, "배송 담당자 조회 성공", DeliveryManagerResponse.from(manager));
    }
}