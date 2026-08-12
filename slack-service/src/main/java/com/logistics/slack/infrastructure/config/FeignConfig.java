package com.logistics.slack.infrastructure.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(3, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor gatewayHeaderRequestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            forwardHeader(request, requestTemplate, "X-User-Id");
            forwardHeader(request, requestTemplate, "X-User-Role");
            forwardHeader(request, requestTemplate, "X-Hub-Id");
            forwardHeader(request, requestTemplate, "X-Company-Id");
        };
    }

    private void forwardHeader(
            HttpServletRequest request,
            feign.RequestTemplate requestTemplate,
            String headerName
    ) {
        String value = request.getHeader(headerName);

        if (value != null && !value.isBlank()) {
            requestTemplate.header(headerName, value);
        }
    }
}
