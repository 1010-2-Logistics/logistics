package com.logistics.inventory.infrastructure.security.filter;

import com.logistics.inventory.global.exception.CustomException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class HeaderAuthenticationFilterTest {
    private final HeaderAuthenticationFilter filter = new HeaderAuthenticationFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("정상적인 HUB_MANAGER 인증 정보면 SecurityContext에 인증 객체 저장")
    void doFilter_hubManager_success() throws Exception {
        // given
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        UUID hubId = UUID.randomUUID();

        mockHttpServletRequest.addHeader("X-User-Id", "1");
        mockHttpServletRequest.addHeader("X-User-Role", "HUB_MANAGER");
        mockHttpServletRequest.addHeader("X-Hub-Id", hubId.toString());

        FilterChain filterChain = (req, res) -> {
        };

        filter.doFilter(mockHttpServletRequest, mockHttpServletResponse, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_HUB_MANAGER");
    }

    @Test
    @DisplayName("HUB_MANAGER인데 hubId가 없으면 인증 정보 검증 실패")
    void doFilter_hubManager_withoutHubId_fail() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        mockHttpServletRequest.addHeader("X-User-Id", "1");
        mockHttpServletRequest.addHeader("X-User-Role", "HUB_MANAGER");

        FilterChain filterChain = (req, res) -> {
        };

        assertThatThrownBy(() -> filter
                .doFilter(mockHttpServletRequest, mockHttpServletResponse, filterChain))
                .isInstanceOf(CustomException.class);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}