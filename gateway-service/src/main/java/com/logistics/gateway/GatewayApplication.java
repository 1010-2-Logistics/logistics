package com.logistics.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EnableDiscoveryClient, EnableEurekaClient 어노테이션 불필요
 * 이유 : Spring Boot 3 이상에서는 자동 등록 지원.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}