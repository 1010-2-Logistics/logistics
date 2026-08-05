package com.logistics.product.application.port;

import java.util.UUID;

import com.logistics.product.application.dto.internal.CompanyExistsResponseDto;
import com.logistics.product.global.response.ApiResponse;

public interface CompanyPort {

	ApiResponse<CompanyExistsResponseDto> companyExistsRequest(UUID companyId);
}
