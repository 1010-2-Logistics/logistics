package com.logistics.delivery.presentation.controller;

import com.logistics.delivery.application.service.DeliveryManagerCommandService;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.global.response.ApiResponse;
import com.logistics.delivery.presentation.controller.dto.request.DeliveryManagerRegisterRequest;
import com.logistics.delivery.presentation.controller.dto.response.DeliveryManagerRegisterResponse;
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

    @DeleteMapping("/{deliveryManagerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long deliveryManagerId) {
        deliveryManagerCommandService.delete(deliveryManagerId);
    }
}