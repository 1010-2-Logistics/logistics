package com.logistics.product.application.dto.internal.response;

import java.util.UUID;

public record HubAuthResponseDto(
		UUID hubId,
		Long hubManagerId
) {

}
