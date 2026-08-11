package com.logistics.delivery.infrastructure.feign.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserAffiliationApiResponse(
        @JsonProperty("SUCCESS") boolean success,
        int code,
        UserAffiliationResponse data,
        String message
) {
    public record UserAffiliationResponse(
            Long userId, String role, UUID companyId, UUID hubId, LocalDateTime updatedAt
    ) {
    }
}
