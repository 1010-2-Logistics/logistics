package com.logistics.order.application.facade;

import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.infrastructure.feign.client.CompanyClient;
import com.logistics.order.infrastructure.feign.client.DeliveryClient;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import com.logistics.order.infrastructure.feign.client.ProductClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {
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
}