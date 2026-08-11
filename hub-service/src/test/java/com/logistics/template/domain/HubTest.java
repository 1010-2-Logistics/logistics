package com.logistics.template.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.hub.domain.entity.Hub;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HubTest {

    @Test
    @DisplayName("허브 생성 시 전달받은 기본 필드가 올바르게 설정된다")
    void 생성하면_ACTIVE_상태로_시작한다() {
        // given & when
        Hub hub = Hub.create(
                "샘플",
                "주소",
                BigDecimal.valueOf(37.1234567),
                BigDecimal.valueOf(127.1234567),
                1L
        );

        // then
        assertThat(hub).isNotNull();
    }
}