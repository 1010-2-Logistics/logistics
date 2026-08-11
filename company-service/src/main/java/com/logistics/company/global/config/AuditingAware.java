package com.logistics.company.global.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.logistics.company.infrastructure.security.principal.UserPrincipal;

@Component
public class AuditingAware implements AuditorAware<Long> {

	@Override
	public Optional<Long> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication == null
			 || !authentication.isAuthenticated()
			 || !(authentication.getPrincipal() instanceof UserPrincipal user)) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(user.getUserId());
	}

}
