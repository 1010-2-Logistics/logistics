package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.service.DeliveryManagerCommandService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.presentation.dto.request.DeliveryManagerRegisterRequest;
import com.logistics.delivery.presentation.dto.request.DeliveryManagerUpdateRequest;
import com.logistics.delivery.presentation.dto.response.DeliveryManagerRegisterResponse;
import com.logistics.delivery.presentation.dto.response.DeliveryManagerUpdateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerCommandController {

    private final DeliveryManagerCommandService deliveryManagerCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DeliveryManagerRegisterResponse> register(
            @Valid @RequestBody DeliveryManagerRegisterRequest request) {
        DeliveryManager manager = deliveryManagerCommandService.register(request.toCommand());
        return ApiResponse.success(201, "배송 담당자 등록 성공", DeliveryManagerRegisterResponse.from(manager));
    }

    @PatchMapping("/{deliveryManagerId}")
    public ApiResponse<DeliveryManagerUpdateResponse> update(
            @PathVariable Long deliveryManagerId,
            @RequestBody DeliveryManagerUpdateRequest request) {
        DeliveryManager manager = deliveryManagerCommandService.update(deliveryManagerId, request.hubId());
        return ApiResponse.success(200, "배송 담당자 수정 성공", DeliveryManagerUpdateResponse.from(manager));
    }

    @DeleteMapping("/{deliveryManagerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long deliveryManagerId) {
        deliveryManagerCommandService.delete(deliveryManagerId);
    }
}