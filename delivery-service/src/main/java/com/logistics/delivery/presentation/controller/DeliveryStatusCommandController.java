package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.service.DeliveryCommandService;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import com.logistics.delivery.presentation.dto.request.DeliveryRouteStatusChangeRequest;
import com.logistics.delivery.presentation.dto.request.DeliveryStatusChangeRequest;
import com.logistics.delivery.presentation.dto.response.DeliveryRouteStatusChangeResponse;
import com.logistics.delivery.presentation.dto.response.DeliveryStatusChangeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name= "Delivery")
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryStatusCommandController {

    private final DeliveryCommandService deliveryCommandService;

    @Operation(
            summary = "배송 상태 변경",
            description = """
                 접근 권한:
                - MASTER: 전체 변경 가능
                - COMPANY_DELIVERY_MANAGER: 본인 배송건만 변경 가능 (DELIVERED로만 전이)
                """
    )
    @PatchMapping("/{deliveryId}")
    public ApiResponse<DeliveryStatusChangeResponse> changeDeliveryStatus(
            @PathVariable UUID deliveryId, @Valid @RequestBody DeliveryStatusChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        var result = deliveryCommandService.changeDeliveryStatus(deliveryId, request.toCommand(), principal);
        return ApiResponse.success(200, "배송 상태 변경 성공", DeliveryStatusChangeResponse.from(result));
    }

    @Operation(
            summary = "배송 경로 상태 변경",
            description = """
                 접근 권한:
                - MASTER: 전체 변경 가능
                - 담당 허브관리자: 담당 허브 소속 경로만 변경 가능
                - 해당 구간 배송담당자: 본인이 배정된 구간만 변경 가능
                """
    )
    @PatchMapping("/{deliveryId}/routes/{routeId}")
    public ApiResponse<DeliveryRouteStatusChangeResponse> changeDeliveryRouteStatus(
            @PathVariable UUID deliveryId, @PathVariable UUID routeId,
            @Valid @RequestBody DeliveryRouteStatusChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        var result = deliveryCommandService.changeDeliveryRouteStatus(deliveryId, routeId, request.toCommand(), principal);
        return ApiResponse.success(200, "배송 경로 상태 변경 성공", DeliveryRouteStatusChangeResponse.of(result));
    }

    @Operation(
            summary = "배송 삭제",
            description = """
                 접근 권한:
                - MASTER: 전체 삭제 가능
                - 담당 허브관리자: 담당 허브 소속 배송만 삭제 가능
                """
    )
    @DeleteMapping("/{deliveryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDelivery(@PathVariable UUID deliveryId, @AuthenticationPrincipal UserPrincipal principal) {
        deliveryCommandService.deleteDelivery(deliveryId, principal);
    }
}