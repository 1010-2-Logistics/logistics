package com.logistics.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserStatus;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void 생성하면_ACTIVE_상태로_시작한다() {
        // given & when
        User user = User.create("샘플");

        // then
        assertThat(user.getName()).isEqualTo("샘플");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void update하면_이름이_바뀐다() {
        // given
        User user = User.create("이전 이름");

        // when
        user.update("새 이름");

        // then
        assertThat(user.getName()).isEqualTo("새 이름");
    }
}
