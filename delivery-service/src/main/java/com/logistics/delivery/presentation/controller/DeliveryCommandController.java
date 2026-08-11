package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.service.DeliveryCommandService;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.presentation.dto.request.DeliveryCreateRequest;
import com.logistics.delivery.presentation.dto.response.DeliveryCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name= "Delivery Internal")
@RestController
@RequestMapping("/internal/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryCommandController {

    private final DeliveryCommandService deliveryCommandService;

    @Operation(
            summary = "배송 생성",
            description = """
                 접근 권한:
                - Order Service 전용 (내부 호출)
                """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DeliveryCreateResponse> createDelivery(@Valid @RequestBody DeliveryCreateRequest request) {
        var result = deliveryCommandService.createDelivery(request.toCommand());
        return ApiResponse.success(201, "배송 생성 성공", DeliveryCreateResponse.from(result));
    }

    @Operation(
            summary = "배송 취소",
            description = """
                 접근 권한:
                - Order Service 전용 (내부 호출)
                """
    )
    @PatchMapping("/order/{orderId}/cancel")
    public ApiResponse<Void> cancelDelivery(@PathVariable UUID orderId) {
        deliveryCommandService.cancelDelivery(orderId);
        return ApiResponse.success(200, "배송 취소 성공", null);
    }
}