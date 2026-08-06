package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.service.DeliveryCommandService;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.presentation.controller.dto.request.DeliveryCreateRequest;
import com.logistics.delivery.presentation.controller.dto.response.DeliveryCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryCommandController {

    private final DeliveryCommandService deliveryCommandService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DeliveryCreateResponse> create(@Valid @RequestBody DeliveryCreateRequest request) {
        Delivery delivery = deliveryCommandService.create(request.toCommand());
        return ApiResponse.success(201, "배송 생성 성공", DeliveryCreateResponse.from(delivery));
    }
}