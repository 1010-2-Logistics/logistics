package com.logistics.product.application.port;

import com.logistics.product.application.dto.internal.response.UserExistsResponseDto;

public interface UserPort {

	UserExistsResponseDto getUserExists(Long userId);
}
