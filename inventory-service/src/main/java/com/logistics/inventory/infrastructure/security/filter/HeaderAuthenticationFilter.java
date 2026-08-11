package com.logistics.inventory.infrastructure.security.filter;

import com.logistics.inventory.infrastructure.security.principal.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final String USER_ROLE_HEADER = "X-User-Role";
	private static final String USER_HUB_ID = "X-Hub-Id";
	private static final String USER_COMPANY_ID = "X-Company-Id";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String userId = request.getHeader(USER_ID_HEADER);
		String userRole = request.getHeader(USER_ROLE_HEADER);
		String hubId = request.getHeader(USER_HUB_ID);
		String companyId = request.getHeader(USER_COMPANY_ID);
		
		UserPrincipal principal = UserPrincipal.from(userId, userRole, hubId, companyId);
		
		if(principal != null) {
			principal.validateRoleConstraints();

			List<SimpleGrantedAuthority> authorities = (principal.getRole() != null)
					? List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRole().name()))
					: Collections.emptyList();
			
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		filterChain.doFilter(request, response);
	}
}
