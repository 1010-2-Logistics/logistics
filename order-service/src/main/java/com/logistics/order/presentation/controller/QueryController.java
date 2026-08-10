package com.logistics.order.presentation.controller;


import com.logistics.order.application.dto.query.OrderSearchQuery;
import com.logistics.order.application.dto.result.OrderDetailResult;
import com.logistics.order.application.dto.result.OrderListResult;
import com.logistics.order.application.service.OrderQueryService;
import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.security.principal.UserPrincipal;
import com.logistics.order.presentation.dto.response.OrderDetailResponseDto;
import com.logistics.order.presentation.dto.response.OrderListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Order")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class QueryController {
    private final OrderQueryService orderQueryService;

    @Operation(
            summary = "주문 단건 조회",
            description = """
                      접근 권한:
                    - MASTER : 모든 주문 단건 조회 가능
                    - HUB_MANAGER : 담당 허브의 주문만 조회 가능
                    - COMPANY_DELIVERY_MANAGER : 본인이 담당하는 주문만 조회 가능
                    - 업체 담당자(COMPANY_MANAGER : 본인 업체의 주문만 단건 조회 가능
                    """
    )
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

    @Operation(
            summary = "주문 목록 조회",
            description = """
                    접근 권한:
                    - 모든 로그인 사용자 조회 가능
                    - 주문자 본인은 자신의 주문만 조회 가능
                    """
    )
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
