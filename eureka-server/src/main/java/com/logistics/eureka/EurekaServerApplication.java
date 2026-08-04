package com.logistics.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer // 이 애플리케이션을 Eureka Server로 활성화
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        // Spring Boot 애플리케이션 실행
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}