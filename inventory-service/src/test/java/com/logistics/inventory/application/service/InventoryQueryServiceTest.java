package com.logistics.inventory.application.service;

import com.logistics.inventory.domain.repository.InventoryQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class InventoryQueryServiceTest {
    UUID inventoryId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    UUID hubId = UUID.randomUUID();

    @Mock
    private InventoryQueryRepository inventoryQueryRepository;

    @InjectMocks
    private InventoryQueryService inventoryQueryService;

    @Nested
    @DisplayName("재고 단건 조회")
    class inventory_detail {
    }

    @Nested
    @DisplayName("재고 전체 조회")
    class inventory_search {

    }

}