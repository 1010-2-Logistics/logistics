package com.logistics.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// 실제 서비스로 복사할 때 TemplateServiceApplication -> {Service}Application 으로 이름 바꾸세요.
@EnableFeignClients
@SpringBootApplication
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
