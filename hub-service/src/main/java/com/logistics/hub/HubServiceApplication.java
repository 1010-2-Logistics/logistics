package com.logistics.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 실제 서비스로 복사할 때 TemplateServiceApplication -> {Service}Application 으로 이름 바꾸세요.
@EnableJpaAuditing
@EnableFeignClients
@SpringBootApplication
public class HubServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubServiceApplication.class, args);
    }
}
