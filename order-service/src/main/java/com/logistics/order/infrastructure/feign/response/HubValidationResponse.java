package com.logistics.order.infrastructure.feign.response;

import java.util.UUID;

// 다른 서비스(예: hub-service) 응답 DTO 예시. 실제 응답 필드에 맞게 수정하세요.
public record HubValidationResponse(UUID hubId, boolean exists) {
}
