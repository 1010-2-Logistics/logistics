package com.logistics.company.application.port;

import com.logistics.company.application.dto.internal.request.CompanyNameUpdateRequestDto;
import com.logistics.company.application.dto.internal.response.CompanyNameUpdateResponseDto;

public interface ProductPort {

	CompanyNameUpdateResponseDto companyNameUpdate(CompanyNameUpdateRequestDto request, String beforeName);
}
