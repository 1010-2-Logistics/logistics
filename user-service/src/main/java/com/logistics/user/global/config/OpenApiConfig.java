package com.logistics.user.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Swagger(OpenAPI) 공통 설정
     */
    @Bean
    public OpenAPI logisticsOpenAPI() {

        String securitySchemeName = "bearerAuth";

        return new OpenAPI()

                /*
                 * JWT 인증 버튼 추가
                 */
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )

                .schemaRequirement(
                        securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                )

                /*
                 * Swagger 상단 정보
                 */
                .info(
                        new Info()
                                .title("Logistics User Service API")
                                .description("""
                                        물류 시스템 User-Service API 문서

                                        인증 방식
                                        - JWT Bearer Token
                                        """)
                                .version("v1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Team Logistics")
                                )
                );
    }
}