package com.logistics.product.infrastructure.security.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final String USER_ROLE_HEADER = "X-User-Role";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String userId = request.getHeader(USER_ID_HEADER);
		String userRole = request.getHeader(USER_ROLE_HEADER);
		
		if(userId != null && !userId.isBlank()) {
			List<SimpleGrantedAuthority> authorities = (userRole != null && !userRole.isBlank())
					? List.of(new SimpleGrantedAuthority(userRole))
					: Collections.emptyList();
			
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		filterChain.doFilter(request, response);
	}

}
