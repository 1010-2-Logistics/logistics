package com.logistics.order.infrastructure.security.auditor;


import com.logistics.order.infrastructure.security.principal.UserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityAuditorAware implements AuditorAware<Long> {
    // @CreatedBy나 @LastModifiedBy를 사용하면 AuditorAware<T> 구현이 필요하고,
    // 현재 principal이 누구인지 auditing infrastructure에 알려주는 역할이라고 설명한다

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {

            return Optional.empty();
        }

        return Optional.of(principal.getUserId());
    }
}
