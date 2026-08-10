package com.logistics.delivery.infrastructure.feign.client;

import com.logistics.delivery.infrastructure.config.FeignConfig;
import com.logistics.delivery.infrastructure.feign.request.UserAffiliationRequest;
import com.logistics.delivery.infrastructure.feign.response.UserAffiliationApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserAffiliationClient {

    @PatchMapping("/internal/v1/users/{userId}/affiliation")
    UserAffiliationApiResponse changeAffiliation(@PathVariable Long userId, @RequestBody UserAffiliationRequest request);
}
