package com.logistics.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.entity.UserStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void 생성하면_PENDING_상태로_시작한다() {
        // given
        UUID companyId = UUID.randomUUID();

        // when
        // COMPANY_MANAGER는 companyId가 필수이고 hubId는 없어야 한다.
        User user = User.create(
                "sample01",
                "encoded-password",
                "U0123456789",
                UserRole.COMPANY_MANAGER,
                companyId,
                null
        );

        // then
        assertThat(user.getUsername())
                .isEqualTo("sample01");

        assertThat(user.getPassword())
                .isEqualTo("encoded-password");

        assertThat(user.getSlackId())
                .isEqualTo("U0123456789");

        assertThat(user.getRole())
                .isEqualTo(UserRole.COMPANY_MANAGER);

        assertThat(user.getStatus())
                .isEqualTo(UserStatus.PENDING);

        assertThat(user.getCompanyId())
                .isEqualTo(companyId);

        assertThat(user.getHubId())
                .isNull();
    }

    @Test
    void Slack_ID를_변경할_수_있다() {
        // given
        User user = User.create(
                "sample01",
                "encoded-password",
                "U0123456789",
                UserRole.MASTER,
                null,
                null
        );

        // when
        user.updateSlackId("U9999999999");

        // then
        assertThat(user.getSlackId())
                .isEqualTo("U9999999999");
    }

    @Test
    void 비밀번호를_변경할_수_있다() {
        // given
        User user = User.create(
                "sample01",
                "old-encoded-password",
                "U0123456789",
                UserRole.MASTER,
                null,
                null
        );

        // when
        user.changePassword("new-encoded-password");

        // then
        assertThat(user.getPassword())
                .isEqualTo("new-encoded-password");
    }

    @Test
    void 사용자를_승인할_수_있다() {
        // given
        User user = User.create(
                "sample01",
                "encoded-password",
                "U0123456789",
                UserRole.MASTER,
                null,
                null
        );

        // when
        user.approve();

        // then
        assertThat(user.getStatus())
                .isEqualTo(UserStatus.APPROVED);
    }

    @Test
    void 사용자를_거절할_수_있다() {
        // given
        User user = User.create(
                "sample01",
                "encoded-password",
                "U0123456789",
                UserRole.MASTER,
                null,
                null
        );

        // when
        user.reject();

        // then
        assertThat(user.getStatus())
                .isEqualTo(UserStatus.REJECTED);
    }

    @Test
    void 이미_승인된_사용자를_다시_승인하면_예외가_발생한다() {
        // given
        User user = User.create(
                "sample01",
                "encoded-password",
                "U0123456789",
                UserRole.MASTER,
                null,
                null
        );

        user.approve();

        // when & then
        assertThatThrownBy(user::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 APPROVED 상태입니다");
    }

    @Test
    void MASTER는_업체나_허브에_소속될_수_없다() {
        // given
        UUID companyId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(
                () -> User.create(
                        "sample01",
                        "encoded-password",
                        "U0123456789",
                        UserRole.MASTER,
                        companyId,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("MASTER는 업체 또는 허브에 소속될 수 없습니다.");
    }

    @Test
    void 업체_역할은_companyId만_가져야_한다() {
        // given
        UUID hubId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(
                () -> User.create(
                        "sample01",
                        "encoded-password",
                        "U0123456789",
                        UserRole.COMPANY_MANAGER,
                        null,
                        hubId
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("업체 역할은 companyId만 가져야 합니다.");
    }
}