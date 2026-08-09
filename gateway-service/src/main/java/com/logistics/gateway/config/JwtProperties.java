package com.logistics.gateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml의 jwt 설정값을 객체로 묶는다.
 * secret은 user-service와 동일한 값이어야 서명 검증이 성립한다.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(

        /**
         * JWT 서명 검증에 사용하는 비밀키. user-service의 jwt.secret과 같은 값.
         */
        String secret,

        /**
         * 토큰 없이 통과시킬 경로 목록.
         */
        List<String> whitelist

) {
}
