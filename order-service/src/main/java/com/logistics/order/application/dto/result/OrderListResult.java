package com.logistics.order.application.dto.result;

import com.logistics.order.domain.entity.Order;
import com.logistics.order.global.response.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public record OrderListResult(
        List<OrderListItemResult> content,
        PageInfo pageInfo
) {
    public static OrderListResult from(Page<Order> orders) {
        List<OrderListItemResult> content = orders.getContent().stream()
                .map(OrderListItemResult::from)
                .toList();

        return new OrderListResult(
                content,
                PageInfo.of(orders)
        );
    }
}
