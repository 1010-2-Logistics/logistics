package com.logistics.order.presentation.controller;

import com.logistics.order.application.dto.command.OrderCancelCommand;
import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.dto.command.OrderDeleteCommand;
import com.logistics.order.application.dto.command.OrderUpdateCommand;
import com.logistics.order.application.dto.result.OrderCancelResult;
import com.logistics.order.application.dto.result.OrderCreateResult;
import com.logistics.order.application.dto.result.OrderUpdateResult;
import com.logistics.order.application.facade.OrderFacade;
import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.presentation.dto.request.OrderCreateRequestDto;
import com.logistics.order.presentation.dto.request.OrderUpdateRequestDto;
import com.logistics.order.presentation.dto.response.OrderCancelResponseDto;
import com.logistics.order.presentation.dto.response.OrderCreateResponseDto;
import com.logistics.order.presentation.dto.response.OrderUpdateResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class CommandController {
    private final OrderFacade orderFacade;

    @PostMapping
    public ApiResponse<OrderCreateResponseDto> createOrder(
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody OrderCreateRequestDto orderCreateRequestDto
    ) {
        OrderCreateCommand orderCreateCommand = new OrderCreateCommand(
                orderCreateRequestDto.endCompanyId(),
                orderCreateRequestDto.productId(),
                orderCreateRequestDto.quantity(),
                orderCreateRequestDto.request()
        );

        OrderCreateResult orderCreateResult = orderFacade.createOrder(
                orderCreateCommand,
                idempotencyKey
        );

        OrderCreateResponseDto orderCreateResponseDto =
                OrderCreateResponseDto.from(orderCreateResult);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "주문 생성 성공",
                orderCreateResponseDto
        );
    }

    @PatchMapping("/{orderId}")
    public ApiResponse<OrderUpdateResponseDto> updateOrder(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody OrderUpdateRequestDto orderUpdateRequestDto
    ) {
        OrderUpdateCommand orderUpdateCommand = new OrderUpdateCommand(
                orderId,
                orderUpdateRequestDto.quantity(),
                orderUpdateRequestDto.request()
        );

        OrderUpdateResult orderUpdateResult = orderFacade.updateOrder(orderUpdateCommand);

        OrderUpdateResponseDto orderUpdateResponseDto = OrderUpdateResponseDto.from(orderUpdateResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "주문 수정 성공",
                orderUpdateResponseDto
        );
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(
            @PathVariable("orderId") UUID orderId
    ) {
        OrderDeleteCommand orderDeleteCommand = new OrderDeleteCommand(orderId);

        orderFacade.deleteOrder(orderDeleteCommand);
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderCancelResponseDto> cancelOrder(
            @PathVariable("orderId") UUID orderId
    ) {
        OrderCancelCommand orderCancelCommand = new OrderCancelCommand(orderId);

        OrderCancelResult orderCancelResult =
                orderFacade.cancelOrder(orderCancelCommand);

        OrderCancelResponseDto orderCancelResponseDto = OrderCancelResponseDto.from(orderCancelResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "주문 취소 성공",
                orderCancelResponseDto
        );
    }
}
