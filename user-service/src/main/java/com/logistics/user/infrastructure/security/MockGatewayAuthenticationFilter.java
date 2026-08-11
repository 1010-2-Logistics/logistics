package com.logistics.user.infrastructure.security;

import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.global.exception.AuthErrorCode;
import com.logistics.user.global.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Gateway가 검증 후 주입한 헤더를 읽어 Authentication을 생성한다.
 * - X-User-Id
 * - X-User-Role
 * - X-Hub-Id
 * - X-Company-Id
 */
@Component
@RequiredArgsConstructor
public class MockGatewayAuthenticationFilter
        extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String HUB_ID_HEADER = "X-Hub-Id";
    private static final String COMPANY_ID_HEADER = "X-Company-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userIdHeader =
                request.getHeader(USER_ID_HEADER);

        String userRoleHeader =
                request.getHeader(USER_ROLE_HEADER);

        /*
         * 인증 헤더가 없는 요청은 인증 객체를 만들지 않는다.
         * 이후 Spring Security가 인증 실패를 처리한다.
         */
        if (userIdHeader == null || userRoleHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try{

            Long userId = parseLong(userIdHeader);
            UserRole role = parseRole(userRoleHeader);

            UUID hubId = parseNullableUuid(
                    request.getHeader(HUB_ID_HEADER)
            );

            UUID companyId = parseNullableUuid(
                    request.getHeader(COMPANY_ID_HEADER)
            );

            AuthenticatedUser principal =
                    new AuthenticatedUser(
                            userId,
                            role,
                            hubId,
                            companyId
                    );

            principal.validateRoleConstraints();

            /*
             * Spring Security는 ROLE_ 접두사가 붙은 권한을
             * 일반적인 역할 권한으로 다룬다.
             */
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + role.name()
                    );

            /*
             * principal에는 현재 사용자 정보를 넣는다.
             * credentials는 이미 Gateway에서 인증됐으므로 null이다.
             */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(authority)
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();

            writeErrorResponse(
                    response,
                    AuthErrorCode.AUTH_INVALID_GATEWAY_HEADER,
                    exception.getMessage()
            );
        }
    }
    private void writeErrorResponse(
            HttpServletResponse response,
            AuthErrorCode errorCode,
            String reason
    ) throws IOException {

        response.setStatus(
                errorCode.getHttpStatus().value()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        ErrorResponse errorResponse =
                ErrorResponse.of(
                        errorCode,
                        reason
                );

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }






    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "X-User-Id 형식이 올바르지 않습니다.",
                    exception
            );
        }
    }

    private UserRole parseRole(String value) {
        try {
            return UserRole.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "X-User-Role 형식이 올바르지 않습니다.",
                    exception
            );
        }
    }

    private UUID parseNullableUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "UUID 헤더 형식이 올바르지 않습니다.",
                    exception
            );
        }
    }
}