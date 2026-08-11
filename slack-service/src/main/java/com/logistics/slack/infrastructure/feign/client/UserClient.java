package com.logistics.slack.infrastructure.feign.client;

import com.logistics.slack.infrastructure.config.FeignConfig;

import com.logistics.slack.global.response.ApiResponse;
import com.logistics.slack.infrastructure.feign.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        configuration = FeignConfig.class
)
public interface UserClient {
    @GetMapping("/internal/v1/users/{userId}")
    ApiResponse<UserInfoResponse> getUser(
            @PathVariable("userId") Long userId
    );
}
