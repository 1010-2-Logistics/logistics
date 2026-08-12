package com.logistics.delivery.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.delivery.domain.entity.Role;
import com.logistics.delivery.infrastructure.security.auditor.SecurityAuditorAware;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityAuditorAwareTest {

    private final SecurityAuditorAware auditorAware = new SecurityAuditorAware();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 인증_컨텍스트가_없으면_시스템_주체로_기록한다() {
        // order-service가 호출하는 내부 API는 인증 컨텍스트가 없다
        SecurityContextHolder.clearContext();

        assertThat(auditorAware.getCurrentAuditor()).contains(0L);
    }

    @Test
    void 인증된_사용자는_해당_사용자_ID로_기록한다() {
        UserPrincipal principal = new UserPrincipal(42L, Role.MASTER, null, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, null));

        assertThat(auditorAware.getCurrentAuditor()).contains(42L);
    }
}