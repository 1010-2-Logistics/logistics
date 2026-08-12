package com.logistics.delivery.infrastructure.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(3, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true);
    }

    /**
     * 응답을 받지 못한 경우(RetryableException)에만 재시도한다. 서버가 상태 코드를
     * 응답했다면 결과가 확정된 것이므로 재시도하지 않는다.
     *
     * <p>delivery가 호출하는 외부 API는 조회(GET)와 목표 상태를 지정하는 소속 변경
     * (PATCH)뿐이라 모두 멱등하다. 따라서 재시도해도 결과가 달라지지 않는다.
     *
     * <p>트랜잭션 안에서 호출되므로 대기가 길어지면 DB 커넥션과 비관적 락을 그만큼
     * 오래 점유한다. 시도 횟수를 2회로 제한한다.
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 2);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
