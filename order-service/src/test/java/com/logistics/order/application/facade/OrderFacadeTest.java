package com.logistics.order.application.facade;

import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.infrastructure.feign.client.CompanyClient;
import com.logistics.order.infrastructure.feign.client.DeliveryClient;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import com.logistics.order.infrastructure.feign.client.ProductClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {
    // TODO : CRUD 이후 진행 예정
    @Mock
    private OrderCommandService orderCommandService;

    @Mock
    private ProductClient productClient;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private DeliveryClient deliveryClient;

    @InjectMocks
    private OrderFacade orderFacade;

    @Nested
    @DisplayName("주문 생성")
    class order_create{
        @Test
        @DisplayName("성공")
        void order_create_success(){

        }

        @Test
        @DisplayName("업체 조회 실패 시 이후 호출 중단")
        void order_create_company(){

        }

        @Test
        @DisplayName("재고 차감 실패 시 배송과 주문 생성 중단")
        void order_create_inventory(){

        }

        @Test
        @DisplayName("배송 생성 실패 시 주문 저장 중단")
        void order_create_fail_save(){

        }
    }

    @Nested
    @DisplayName("주문 수정")
    class order_update{

    }

    @Nested
    @DisplayName("주문 삭제")
    class order_delete{

    }
}