package com.logistics.ai.presentation.interceptor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.logistics.ai.infrastructure.security.principal.UserPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if(handler instanceof HandlerMethod handlerMethod) {
			if(handlerMethod.getMethodAnnotation(NoAuthentication.class) != null) {
				return true;
			}
		}
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if(authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
			principal.validateRoleConstraints();
		}
		
		return true;
	}
	
}
