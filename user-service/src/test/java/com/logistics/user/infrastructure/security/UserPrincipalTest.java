package com.logistics.user.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logistics.user.domain.entity.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserPrincipalTest {

    @Test
    void MASTER는_소속_정보가_없으면_정상이다() {
        // given
        UserPrincipal user = new UserPrincipal(
                1L,
                UserRole.MASTER,
                null,
                null
        );

        // when & then
        assertThatCode(user::validateRoleConstraints)
                .doesNotThrowAnyException();
    }

    @Test
    void MASTER가_허브_소속을_가지면_예외가_발생한다() {
        // given
        UserPrincipal user = new UserPrincipal(
                1L,
                UserRole.MASTER,
                UUID.randomUUID(),
                null
        );

        // when & then
        assertThatThrownBy(user::validateRoleConstraints)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void MASTER가_업체_소속을_가지면_예외가_발생한다() {
        // given
        UserPrincipal user = new UserPrincipal(
                1L,
                UserRole.MASTER,
                null,
                UUID.randomUUID()
        );

        // when & then
        assertThatThrownBy(user::validateRoleConstraints)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void HUB_MANAGER는_hubId가_있으면_정상이다() {
        // given
        UserPrincipal user = new UserPrincipal(
                2L,
                UserRole.HUB_MANAGER,
                UUID.randomUUID(),
                null
        );

        // when & then
        assertThatCode(user::validateRoleConstraints)
                .doesNotThrowAnyException();
    }

    @Test
    void HUB_MANAGER의_hubId가_없으면_예외가_발생한다() {
        // given
        UserPrincipal user = new UserPrincipal(
                2L,
                UserRole.HUB_MANAGER,
                null,
                null
        );

        // when & then
        assertThatThrownBy(user::validateRoleConstraints)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void COMPANY_MANAGER는_hubId와_companyId가_있으면_정상이다() {
        // given
        UserPrincipal user = new UserPrincipal(
                3L,
                UserRole.COMPANY_MANAGER,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        // when & then
        assertThatCode(user::validateRoleConstraints)
                .doesNotThrowAnyException();
    }

    @Test
    void COMPANY_MANAGER의_companyId가_없으면_예외가_발생한다() {
        // given
        UserPrincipal user = new UserPrincipal(
                3L,
                UserRole.COMPANY_MANAGER,
                UUID.randomUUID(),
                null
        );

        // when & then
        assertThatThrownBy(user::validateRoleConstraints)
                .isInstanceOf(IllegalArgumentException.class);
    }
}