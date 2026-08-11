package com.logistics.inventory.infrastructure.security.filter;

import com.logistics.inventory.domain.entity.Role;
import com.logistics.inventory.infrastructure.security.properties.InternalServiceProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class ServiceAuthenticationFilter extends OncePerRequestFilter {
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";
    private static final String SERVICE_KEY_HEADER = "X-Service-Key";

    private final InternalServiceProperties properties;

    // shouldNotFilter가 하는 일 (HeaderAuthenticationFilter와 책임 안 섞이게)
    // /api/**       : 무시
    // /internal/**  : 검사
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String serviceName = request.getHeader(SERVICE_NAME_HEADER);

        String serviceKey = request.getHeader(SERVICE_KEY_HEADER);

        if (Role.ORDER_SERVICE.name().equals(serviceName)
                && properties.serviceKey().equals(serviceKey)) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            serviceName,
                            null,
                            List.of(new SimpleGrantedAuthority(
                                            "ROLE_" + Role.ORDER_SERVICE.name()
                                    )
                            )
                    );

            SecurityContext context = SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }

        filterChain.doFilter(request, response);
    }
}
