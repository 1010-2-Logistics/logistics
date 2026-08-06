package com.logistics.company.application.port;

import com.logistics.company.application.dto.internal.request.UserRoleUpdateRequestDto;
import com.logistics.company.application.dto.internal.response.UserExistsResponseDto;
import com.logistics.company.application.dto.internal.response.UserRoleUpdateResponseDto;

public interface UserPort {
	
	UserRoleUpdateResponseDto companyManagerRoleUpdateRequest(UserRoleUpdateRequestDto request);
	
	UserExistsResponseDto userExistsRequest(Long userId);
}
