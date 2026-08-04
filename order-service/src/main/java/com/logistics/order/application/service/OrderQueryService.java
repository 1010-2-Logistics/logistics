package com.logistics.order.application.service;

import com.logistics.order.application.dto.query.OrderSearchQuery;
import com.logistics.order.application.dto.result.OrderDetailResult;
import com.logistics.order.application.dto.result.OrderListResult;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.repository.OrderQueryRepository;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderQueryRepository orderQueryRepository;

    // TODO : update ErrorCode
    public OrderDetailResult getOrder(UUID orderId) {
        Order order = orderQueryRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.SAMPLE_NOT_FOUND));

        return OrderDetailResult.from(order);
    }

    public OrderListResult getOrders(OrderSearchQuery orderSearchQuery) {
        return null;
    }
}
