package com.logistics.order.presentation.controller;


import com.logistics.order.application.dto.query.OrderSearchQuery;
import com.logistics.order.application.dto.result.OrderDetailResult;
import com.logistics.order.application.dto.result.OrderListResult;
import com.logistics.order.application.service.OrderQueryService;
import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.security.principal.UserPrincipal;
import com.logistics.order.presentation.dto.response.OrderDetailResponseDto;
import com.logistics.order.presentation.dto.response.OrderListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class QueryController {
    private final OrderQueryService orderQueryService;

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponseDto> getOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("orderId") UUID orderId
    ) {
        OrderDetailResult orderDetailResult = orderQueryService.getOrder(
                orderId,
                principal.toAuthenticatedUser()
        );

        OrderDetailResponseDto orderDetailResponseDto = OrderDetailResponseDto.from(orderDetailResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "주문 단건 조회 성공",
                orderDetailResponseDto
        );
    }

    @GetMapping
    public ApiResponse<OrderListResponseDto> getOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID endCompanyId,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        OrderSearchQuery orderSearchQuery = new OrderSearchQuery(
                productId,
                endCompanyId,
                sort,
                page,
                size
        );

        OrderListResult orderListResult = orderQueryService.getOrders(
                orderSearchQuery,
                principal.toAuthenticatedUser()
        );

        OrderListResponseDto orderListResponseDto = OrderListResponseDto.from(orderListResult);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "주문 목록 조회 성공",
                orderListResponseDto
        );
    }
}
