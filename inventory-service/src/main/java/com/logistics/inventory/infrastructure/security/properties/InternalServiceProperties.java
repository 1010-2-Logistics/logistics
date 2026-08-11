package com.logistics.inventory.infrastructure.security.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.auth")
public record InternalServiceProperties(
        String serviceKey
) {

}
