package com.logistics.hubRoute.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.hubRoute.domain.entity.HubRoute;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HubRouteTest {

    @Test
    @DisplayName("생성하면 모든 필드가 정상적으로 초기화된다")
    void 생성하면_모든_필드가_정상적으로_초기화된다() {
        // given
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        Integer duration = 120;
        BigDecimal distance = BigDecimal.valueOf(150.50);
        Long createdBy = 1L;

        // when
        HubRoute hubRoute = HubRoute.create(startHubId, endHubId, duration, distance, createdBy);

        // then
        assertThat(hubRoute.getStartHubId()).isEqualTo(startHubId);
        assertThat(hubRoute.getEndHubId()).isEqualTo(endHubId);
        assertThat(hubRoute.getDuration()).isEqualTo(duration);
        assertThat(hubRoute.getDistance()).isEqualTo(distance);
        assertThat(hubRoute.getCreatedBy()).isEqualTo(createdBy);
    }

    @Test
    @DisplayName("update하면 허브경로 정보가 변경된다")
    void update하면_허브경로_정보가_변경된다() {
        // given
        UUID startHubId = UUID.randomUUID();
        UUID endHubId = UUID.randomUUID();
        HubRoute hubRoute = HubRoute.create(startHubId, endHubId, 120, BigDecimal.valueOf(150.50), 1L);

        UUID newStartHubId = UUID.randomUUID();
        UUID newEndHubId = UUID.randomUUID();
        Integer newDuration = 180;
        BigDecimal newDistance = BigDecimal.valueOf(200.00);

        // when
        hubRoute.update(newStartHubId, newEndHubId, newDuration, newDistance);

        // then
        assertThat(hubRoute.getStartHubId()).isEqualTo(newStartHubId);
        assertThat(hubRoute.getEndHubId()).isEqualTo(newEndHubId);
        assertThat(hubRoute.getDuration()).isEqualTo(newDuration);
        assertThat(hubRoute.getDistance()).isEqualTo(newDistance);
    }
}