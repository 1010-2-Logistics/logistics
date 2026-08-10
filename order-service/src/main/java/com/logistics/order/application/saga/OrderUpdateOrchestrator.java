package com.logistics.order.application.saga;


import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.infrastructure.feign.client.InventoryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderUpdateOrchestrator {
    private final OrderCommandService orderCommandService;
    private final InventoryClient inventoryClient;


}
