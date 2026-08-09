package com.logistics.inventory.application.dto.result;

import java.util.UUID;

// Controller 응답 DTO가 아니라 Port가 외부 서비스에서 받아 Application에 전달하는 결과
public record ProductExistsResponseDto(
        UUID productId,
        boolean exists
) {
}
