package com.logistics.order.infrastructure.feign.client;


import com.logistics.order.global.response.ApiResponse;
import com.logistics.order.infrastructure.feign.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient
{
    @GetMapping("/internal/v1/users/{userId}")
    ApiResponse<UserInfoResponse> getUser(
            @PathVariable("userId") Long userId
    );
}
