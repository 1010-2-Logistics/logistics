package com.logistics.company.infrastructure.feign.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

// 다른 서비스(예: hub-service) 응답 DTO 예시. 실제 응답 필드에 맞게 수정하세요.
public record HubValidationResponse(
		UUID hubId,
		String name,
		String hubAddress
) {
}
