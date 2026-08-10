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
import com.logistics.order.infrastructure.security.principal.UserPrincipal;
import com.logistics.order.presentation.dto.request.OrderCreateRequestDto;
import com.logistics.order.presentation.dto.request.OrderUpdateRequestDto;
import com.logistics.order.presentation.dto.response.OrderCancelResponseDto;
import com.logistics.order.presentation.dto.response.OrderCreateResponseDto;
import com.logistics.order.presentation.dto.response.OrderUpdateResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Tag(name = "Order")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class CommandController {
    private final OrderFacade orderFacade;

    @Operation(
            summary = "주문 생성",
            description = """
                      접근 권한:
                    - 모든 로그인 사용자
                    """
    )
    @PostMapping
    public ApiResponse<OrderCreateResponseDto> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
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
                idempotencyKey,
                principal.toAuthenticatedUser()
        );

        OrderCreateResponseDto orderCreateResponseDto =
                OrderCreateResponseDto.from(orderCreateResult);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "주문 생성 성공",
                orderCreateResponseDto
        );
    }

    @Operation(
            summary = "주문 수정",
            description = """
                      접근 권한:
                    - MASTER
                    - HUB_MANAGER : 담당 허브 주문만 수정 가능
                    """
    )
    @PatchMapping("/{orderId}")
    public ApiResponse<OrderUpdateResponseDto> updateOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody OrderUpdateRequestDto orderUpdateRequestDto
    ) {
        OrderUpdateCommand orderUpdateCommand = new OrderUpdateCommand(
                orderId,
                orderUpdateRequestDto.quantity(),
                orderUpdateRequestDto.request()
        );

        OrderUpdateResult orderUpdateResult = orderFacade.updateOrder(
                orderUpdateCommand,
                principal.toAuthenticatedUser()
        );

        OrderUpdateResponseDto orderUpdateResponseDto = OrderUpdateResponseDto.from(orderUpdateResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "주문 수정 성공",
                orderUpdateResponseDto
        );
    }

    @Operation(
            summary = "주문 삭제",
            description = """
                      접근 권한:
                    - MASTER : 모든 주문 삭제 가능
                    - HUB_MANAGER : 담당 허브의 주문만 삭제 가능
                    """
    )
    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("orderId") UUID orderId
    ) {
        OrderDeleteCommand orderDeleteCommand = new OrderDeleteCommand(orderId);

        orderFacade.deleteOrder(
                orderDeleteCommand,
                principal.toAuthenticatedUser()
        );
    }

    @Operation(
            summary = "주문 취소",
            description = """
                      접근 권한:
                    - MASTER
                    - HUB_MANAGER : 담당 허브의 주문만 삭제 가능
                    """
    )
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderCancelResponseDto> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("orderId") UUID orderId
    ) {
        OrderCancelCommand orderCancelCommand = new OrderCancelCommand(orderId);

        OrderCancelResult orderCancelResult = orderFacade.cancelOrder(
                orderCancelCommand,
                principal.toAuthenticatedUser()
        );

        OrderCancelResponseDto orderCancelResponseDto = OrderCancelResponseDto.from(orderCancelResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "주문 취소 성공",
                orderCancelResponseDto
        );
    }
}
