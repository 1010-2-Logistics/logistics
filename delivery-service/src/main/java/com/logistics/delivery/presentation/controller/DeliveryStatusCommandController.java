package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.service.DeliveryCommandService;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.presentation.controller.dto.request.DeliveryRouteStatusChangeRequest;
import com.logistics.delivery.presentation.controller.dto.request.DeliveryStatusChangeRequest;
import com.logistics.delivery.presentation.controller.dto.response.DeliveryRouteStatusChangeResponse;
import com.logistics.delivery.presentation.controller.dto.response.DeliveryStatusChangeResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryStatusCommandController {

    private final DeliveryCommandService deliveryCommandService;

    @PatchMapping("/{deliveryId}")
    public ApiResponse<DeliveryStatusChangeResponse> changeStatus(
            @PathVariable UUID deliveryId, @Valid @RequestBody DeliveryStatusChangeRequest request) {
        var result = deliveryCommandService.changeStatus(deliveryId, request.toCommand());
        return ApiResponse.success(200, "배송 상태 변경 성공", DeliveryStatusChangeResponse.from(result));
    }

    @PatchMapping("/{deliveryId}/routes/{routeId}")
    public ApiResponse<DeliveryRouteStatusChangeResponse> changeRouteStatus(
            @PathVariable UUID deliveryId, @PathVariable UUID routeId,
            @Valid @RequestBody DeliveryRouteStatusChangeRequest request) {
        var result = deliveryCommandService.changeRouteStatus(deliveryId, routeId, request.toCommand());
        return ApiResponse.success(200, "배송 경로 상태 변경 성공", DeliveryRouteStatusChangeResponse.of(result));
    }

    @DeleteMapping("/{deliveryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID deliveryId) {
        deliveryCommandService.delete(deliveryId);
    }
}