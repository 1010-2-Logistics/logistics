package com.logistics.template.sample.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.template.domain.entity.Sample;
import com.logistics.template.domain.entity.SampleStatus;
import org.junit.jupiter.api.Test;

class SampleTest {

    @Test
    void 생성하면_ACTIVE_상태로_시작한다() {
        // given & when
        Sample sample = Sample.create("샘플");

        // then
        assertThat(sample.getName()).isEqualTo("샘플");
        assertThat(sample.getStatus()).isEqualTo(SampleStatus.ACTIVE);
    }

    @Test
    void update하면_이름이_바뀐다() {
        // given
        Sample sample = Sample.create("이전 이름");

        // when
        sample.update("새 이름");

        // then
        assertThat(sample.getName()).isEqualTo("새 이름");
    }
}
