package com.logistics.product.application.port;

import java.util.List;
import java.util.UUID;

import com.logistics.product.application.dto.internal.response.CompanyExistsResponseDto;

public interface CompanyPort {

	CompanyExistsResponseDto companyExistsRequest(UUID companyId);
	
	List<UUID> companyIdsByHubIdRequest(UUID hubID);
}
