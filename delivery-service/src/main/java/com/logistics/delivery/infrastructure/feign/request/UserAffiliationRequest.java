package com.logistics.delivery.infrastructure.feign.request;

import java.util.UUID;

public record UserAffiliationRequest(String role, UUID companyId, UUID hubId) {
}
