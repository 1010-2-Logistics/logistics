package com.logistics.template.sample.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.template.domain.entity.Inventory;
import com.logistics.template.domain.entity.InventoryStatus;
import org.junit.jupiter.api.Test;

class SampleTest {

    @Test
    void 생성하면_ACTIVE_상태로_시작한다() {
        // given & when
        Inventory sample = Inventory.create("샘플");

        // then
        assertThat(sample.getName()).isEqualTo("샘플");
        assertThat(sample.getStatus()).isEqualTo(InventoryStatus.ACTIVE);
    }

    @Test
    void update하면_이름이_바뀐다() {
        // given
        Inventory sample = Inventory.create("이전 이름");

        // when
        sample.update("새 이름");

        // then
        assertThat(sample.getName()).isEqualTo("새 이름");
    }
}
