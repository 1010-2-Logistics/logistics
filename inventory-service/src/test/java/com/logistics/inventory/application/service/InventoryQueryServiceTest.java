package com.logistics.inventory.application.service;

import com.logistics.inventory.application.dto.query.InventorySearchQuery;
import com.logistics.inventory.application.dto.result.InventoryDetailResult;
import com.logistics.inventory.application.dto.result.InventoryListResult;
import com.logistics.inventory.domain.entity.Inventory;
import com.logistics.inventory.domain.repository.InventoryQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


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
        @Test
        @DisplayName("재고 단건 조회 성공")
        void inventory_detail_success() {
            Inventory inventory = Inventory.create(
                    productId,
                    hubId,
                    100
            );
            given(inventoryQueryRepository.findByInventoryIdAndDeletedAtIsNull(inventoryId)).willReturn(Optional.of(inventory));

            InventoryDetailResult result = inventoryQueryService.getInventory(inventoryId);

            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.hubId()).isEqualTo(hubId);
            assertThat(result.stock()).isEqualTo(100);

            verify(inventoryQueryRepository).findByInventoryIdAndDeletedAtIsNull(inventoryId);
        }

        @Test
        @DisplayName("존재하지 않는 재고 조회 시 예외")
        void inventory_detail_not_found() {

        }
    }

    @Nested
    @DisplayName("재고 전체 조회")
    class inventory_search {
        @Test
        @DisplayName("재고 목록 조회 성공")
        void inventory_search_success() {
            InventorySearchQuery inventorySearchQuery = new InventorySearchQuery(
                    productId,
                    hubId,
                    "createdAt",
                    0,
                    10
            );
            Inventory inventory = Inventory.create(
                    productId,
                    hubId,
                    100
            );
            Page<Inventory> inventories = new PageImpl<>(List.of(inventory));

            given(inventoryQueryRepository.search(
                    eq(productId),
                    eq(hubId),
                    any(Pageable.class)
            )).willReturn(inventories);

            InventoryListResult inventoryListResult = inventoryQueryService.searchInventory(inventorySearchQuery);

            assertThat(inventoryListResult.content().get(0).productId()).isEqualTo(productId);
            assertThat(inventoryListResult.content().get(0).hubId()).isEqualTo(hubId);
            assertThat(inventoryListResult.content().get(0).stock()).isEqualTo(100);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(inventoryQueryRepository).search(
                    eq(productId),
                    eq(hubId),
                    pageableCaptor.capture()
            );

            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort()
                    .getOrderFor("createdAt")
                    .isDescending()
            ).isTrue();
        }

        @Test
        @DisplayName("페이지 번호가 null이면 기본값 0")
        void inventory_search_default_page() {
            InventorySearchQuery inventorySearchQuery = new InventorySearchQuery(
                    null,
                    null,
                    "createdAt",
                    null,
                    10
            );

            given(inventoryQueryRepository.search(
                    any(),
                    any(),
                    any(Pageable.class)
            )).willReturn(Page.empty());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            inventoryQueryService.searchInventory(inventorySearchQuery);

            verify(inventoryQueryRepository).search(
                    any(),
                    any(),
                    pageableCaptor.capture()
            );

            assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        }

        @Test
        @DisplayName("페이지 크기가 null이면 기본값 10")
        void inventory_search_default_size() {
            InventorySearchQuery inventorySearchQuery = new InventorySearchQuery(
                    null,
                    null,
                    "createdAt",
                    0,
                    null
            );

            given(inventoryQueryRepository.search(
                    any(),
                    any(),
                    any(Pageable.class)
            )).willReturn(Page.empty());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            inventoryQueryService.searchInventory(inventorySearchQuery);

            verify(inventoryQueryRepository).search(
                    any(),
                    any(),
                    pageableCaptor.capture()
            );

            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("허용되지 않은 페이지 크기면 기본값 10")
        void inventory_search_invalid_size() {

        }

        @Test
        @DisplayName("정렬 조건이 null이면 createdAt 적용")
        void inventory_search_default_sort() {

        }

        @Test
        @DisplayName("페이지 번호가 음수이면 예외")
        void inventory_search_negative_page() {

        }
    }
}