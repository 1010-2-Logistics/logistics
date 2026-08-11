package com.logistics.order.application.service;

import com.logistics.order.application.dto.query.OrderSearchQuery;
import com.logistics.order.application.dto.result.DeliveryGetResult;
import com.logistics.order.application.dto.result.OrderDetailResult;
import com.logistics.order.application.dto.result.OrderListResult;
import com.logistics.order.application.port.DeliveryPort;
import com.logistics.order.domain.entity.Order;
import com.logistics.order.domain.entity.Role;
import com.logistics.order.domain.repository.OrderQueryRepository;
import com.logistics.order.global.exception.CustomException;
import com.logistics.order.global.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {
    private final OrderQueryRepository orderQueryRepository;
    private final OrderAuthorizationService orderAuthorizationService;
    private final DeliveryPort deliveryPort;

    public OrderDetailResult getOrder(
            UUID orderId,
            AuthenticatedUser authenticatedUser
    ) {
        Order order = orderQueryRepository.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        DeliveryGetResult delivery = deliveryPort.getDelivery(order.getDeliveryId());

        orderAuthorizationService.validateReadAccess(
                authenticatedUser,
                order,
                delivery.startHubId(),
                delivery.deliveryManagerId()
        );

        return OrderDetailResult.from(order);
    }

    public OrderListResult getOrders(
            OrderSearchQuery orderSearchQuery,
            AuthenticatedUser authenticatedUser
    ) {
        int page = validatePage(orderSearchQuery.page());
        int size = validateSize(orderSearchQuery.size());
        String sortProperty = validateSort(orderSearchQuery.sort());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sortProperty)
        );

        Page<Order> orders;

        if (authenticatedUser.role() == Role.COMPANY_MANAGER) {
            orders = orderQueryRepository.searchByCreatedBy(
                    authenticatedUser.userId(),
                    orderSearchQuery.productId(),
                    orderSearchQuery.endCompanyId(),
                    pageable
            );
        } else if (authenticatedUser.role() == Role.HUB_DELIVERY_MANAGER
                || authenticatedUser.role() == Role.COMPANY_DELIVERY_MANAGER) {
            Page<Order> searchedOrders = orderQueryRepository.search(
                    orderSearchQuery.productId(),
                    orderSearchQuery.endCompanyId(),
                    pageable
            );

            List<Order> filteredOrders = searchedOrders.getContent().stream()
                    .filter(order -> isAssignedDeliveryManager(
                            order,
                            authenticatedUser.userId()
                    )).toList();

            orders = new PageImpl<>(
                    filteredOrders,
                    pageable,
                    filteredOrders.size()
            );
        } else {
            orders = orderQueryRepository.search(
                    orderSearchQuery.productId(),
                    orderSearchQuery.endCompanyId(),
                    pageable
            );
        }

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

    private int validateSize(Integer size) {
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

    private boolean isAssignedDeliveryManager(
            Order order,
            Long userId
    ) {
        DeliveryGetResult delivery = deliveryPort.getDelivery(order.getDeliveryId());

        return Objects.equals(
                delivery.deliveryManagerId(),
                userId
        );
    }
}
