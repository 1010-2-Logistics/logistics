package com.logistics.order.application.service;

import com.logistics.order.application.dto.auth.AuthenticatedUser;
import com.logistics.order.application.dto.query.OrderSearchQuery;
import com.logistics.order.application.dto.result.OrderDetailResult;
import com.logistics.order.application.dto.result.OrderListResult;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.repository.OrderQueryRepository;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {
    private final OrderQueryRepository orderQueryRepository;

    public OrderDetailResult getOrder(
            UUID orderId,
            AuthenticatedUser authenticatedUser
    ) {
        Order order = orderQueryRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        return OrderDetailResult.from(order);
    }

    public OrderListResult getOrders(OrderSearchQuery orderSearchQuery) {
        int page = validatePage(orderSearchQuery.page());
        int size = normalizeSize(orderSearchQuery.size());
        String sortProperty = validateSort(orderSearchQuery.sort());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sortProperty)
        );

        Page<Order> orders = orderQueryRepository.search(
                orderSearchQuery.productId(),
                orderSearchQuery.endCompanyId(),
                pageable
        );

        return OrderListResult.from(orders);
    }

    private int validatePage(Integer page) {
        if (page == null) {
            return 0;
        }

        if (page < 0) {
            throw new CustomException(OrderErrorCode.ORDER_INVALID_REQUEST);
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return 10;
        }

        if (size == 10 || size == 30 || size == 50) {
            return size;
        }
        return 10;
    }

    private String validateSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "createdAt";
        }

        if ("createdAt".equals(sort) || "updatedAt".equals(sort)) {
            return sort;
        }

        throw new CustomException(OrderErrorCode.ORDER_INVALID_REQUEST);
    }

}
