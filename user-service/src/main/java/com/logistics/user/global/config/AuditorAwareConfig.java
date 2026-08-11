package com.logistics.user.global.config;

import com.logistics.user.infrastructure.security.UserPrincipal;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class AuditorAwareConfig {

    /**
     * 현재 요청을 수행한 사용자 ID 반환
     *
     * @CreatedBy / @LastModifiedBy에서 사용
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            /*
             * 인증 정보가 없는 경우
             * createdBy / updatedBy는 null로 처리
             *
             * 예:
             * - 회원가입
             * - 애플리케이션 시작 시 MASTER 자동 생성
             */
            if (authentication == null
                    || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal =
                    authentication.getPrincipal();

            /*
             * 프로젝트에서 사용하는 인증 객체가 아닌 경우
             * auditing 대상자를 결정하지 않는다.
             */
            if (!(principal instanceof UserPrincipal currentUser)) {
                return Optional.empty();
            }

            return Optional.of(
                    currentUser.userId()
            );
        };
    }
}