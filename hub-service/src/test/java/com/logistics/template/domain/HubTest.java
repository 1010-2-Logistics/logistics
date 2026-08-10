package com.logistics.template.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.hub.domain.entity.Hub;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class HubTest {

    @Test
    void 생성하면_ACTIVE_상태로_시작한다() {
        // given & when
        Hub hub = Hub.create("샘플",
                "주소",
                BigDecimal.valueOf(37.1234567),
                BigDecimal.valueOf(127.1234567),
                1L);

        // then
        assertThat(hub.getHubName()).isEqualTo("샘플");
        assertThat(hub.getHubAddress()).isEqualTo("주소");
        assertThat(hub.getLatitude()).isEqualTo(BigDecimal.valueOf(37.1234567));
        assertThat(hub.getLongitude()).isEqualTo(BigDecimal.valueOf(127.1234567));
    }
}
