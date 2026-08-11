package com.logistics.user.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.logistics.user.domain.entity.UserRole;
import jakarta.servlet.FilterChain;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

class MockGatewayAuthenticationFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MockGatewayAuthenticationFilter filter =
            new MockGatewayAuthenticationFilter(objectMapper);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 정상적인_GATEWAY_헤더가_있으면_Authentication을_생성한다()
            throws Exception {

        // given
        UUID hubId = UUID.randomUUID();

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader("X-User-Id", "10");
        request.addHeader("X-User-Role", "HUB_MANAGER");
        request.addHeader("X-Hub-Id", hubId.toString());

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                (req, res) -> {
                };

        // when
        filter.doFilter(
                request,
                response,
                filterChain
        );

        // then
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertThat(authentication)
                .isNotNull();

        assertThat(authentication.isAuthenticated())
                .isTrue();

        UserPrincipal principal =
                (UserPrincipal)
                        authentication.getPrincipal();

        assertThat(principal.userId())
                .isEqualTo(10L);

        assertThat(principal.role())
                .isEqualTo(UserRole.HUB_MANAGER);

        assertThat(principal.hubId())
                .isEqualTo(hubId);

        assertThat(principal.companyId())
                .isNull();

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_HUB_MANAGER");
    }
}