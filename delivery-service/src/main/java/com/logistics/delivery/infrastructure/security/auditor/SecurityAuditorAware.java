package com.logistics.delivery.infrastructure.security.auditor;

import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditorAware implements AuditorAware<Long> {

    // 내부 API(order-service의 배송 생성 등)는 인증 컨텍스트가 없다.
    // created_by가 NOT NULL이므로 시스템 주체로 기록한다.
    private static final Long SYSTEM_AUDITOR = 0L;

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return Optional.of(SYSTEM_AUDITOR);
        }
        return Optional.of(principal.getUserId());
    }
}