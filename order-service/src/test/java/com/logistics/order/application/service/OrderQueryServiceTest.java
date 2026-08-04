package com.logistics.order.application.service;

import com.logistics.order.domain.repository.OrderQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {
    @Mock
    OrderQueryRepository orderQueryRepository;

    @Nested
    @DisplayName("주문 단건 조회")
    class order_getOne {

    }

    @Nested
    @DisplayName("주문 전체 조회")
    class order_getAll {

    }
}