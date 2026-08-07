package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.service.DeliveryQueryService;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.presentation.controller.dto.response.DeliveryInternalResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryInternalQueryController {

    private final DeliveryQueryService deliveryQueryService;

    @GetMapping("/{deliveryId}")
    public ApiResponse<DeliveryInternalResponse> getInternal(@PathVariable UUID deliveryId) {
        var result = deliveryQueryService.getInternal(deliveryId);
        return ApiResponse.success(200, "배송 조회 성공", DeliveryInternalResponse.from(result));
    }
}