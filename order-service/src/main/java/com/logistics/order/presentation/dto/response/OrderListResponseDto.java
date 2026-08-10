package com.logistics.order.presentation.dto.response;

import com.logistics.order.application.dto.result.OrderListResult;
import com.logistics.order.global.response.PageInfo;

import java.util.List;

public record OrderListResponseDto(
        List<OrderListItemResponseDto> content,
        PageInfo pageInfo
) {
    public static OrderListResponseDto from(OrderListResult orderListResult){
        List<OrderListItemResponseDto> content = orderListResult.content().stream()
                .map(OrderListItemResponseDto::from)
                .toList();

        return new OrderListResponseDto(content, orderListResult.pageInfo());
    }
}
