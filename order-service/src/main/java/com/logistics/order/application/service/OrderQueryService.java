package com.logistics.order.application.service;

import com.logistics.order.application.dto.query.OrderSearchQuery;
import com.logistics.order.application.dto.result.OrderDetailResult;
import com.logistics.order.application.dto.result.OrderListResult;
import com.logistics.order.domain.repository.OrderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderQueryRepository orderQueryRepository;

    public OrderDetailResult getOrder(UUID orderId) {
        return null;
    }

    public OrderListResult getOrders(OrderSearchQuery orderSearchQuery) {
        return null;
    }
}
