package com.logistics.delivery.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeliveryManagerAssignmentStateTest {

    @Test
    void init하면_lastAssignedSequence가_마이너스1로_초기화된다() {
        // when
        DeliveryManagerAssignmentState state = DeliveryManagerAssignmentState.init(ManagerType.HUB_DELIVERY_MANAGER, null);

        // then
        assertThat(state.getManagerType()).isEqualTo(ManagerType.HUB_DELIVERY_MANAGER);
        assertThat(state.getHubId()).isNull();
        assertThat(state.getLastAssignedSequence()).isEqualTo(-1);
        assertThat(state.getLastAssignedManagerId()).isNull();
    }

    @Test
    void init은_hubId를_그대로_보관한다() {
        // given
        UUID hubId = UUID.randomUUID();

        // when
        DeliveryManagerAssignmentState state = DeliveryManagerAssignmentState.init(ManagerType.COMPANY_DELIVERY_MANAGER, hubId);

        // then
        assertThat(state.getHubId()).isEqualTo(hubId);
    }

    @Test
    void assign하면_lastAssignedSequence와_lastAssignedManagerId가_해당_담당자_값으로_갱신된다() {
        // given
        DeliveryManagerAssignmentState state = DeliveryManagerAssignmentState.init(ManagerType.HUB_DELIVERY_MANAGER, null);
        DeliveryManager manager = DeliveryManager.create(7L, null, "U07", ManagerType.HUB_DELIVERY_MANAGER, 3);

        // when
        state.assign(manager);

        // then
        assertThat(state.getLastAssignedSequence()).isEqualTo(3);
        assertThat(state.getLastAssignedManagerId()).isEqualTo(7L);
    }

    @Test
    void assign을_여러번_호출하면_가장_최근_값으로_계속_덮어써진다() {
        // given
        DeliveryManagerAssignmentState state = DeliveryManagerAssignmentState.init(ManagerType.HUB_DELIVERY_MANAGER, null);
        DeliveryManager first = DeliveryManager.create(1L, null, "U01", ManagerType.HUB_DELIVERY_MANAGER, 0);
        DeliveryManager second = DeliveryManager.create(2L, null, "U02", ManagerType.HUB_DELIVERY_MANAGER, 1);

        // when
        state.assign(first);
        state.assign(second);

        // then
        assertThat(state.getLastAssignedSequence()).isEqualTo(1);
        assertThat(state.getLastAssignedManagerId()).isEqualTo(2L);
    }
}
