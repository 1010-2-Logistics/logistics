package com.logistics.company.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.company.application.dto.internal.request.UserRoleUpdateRequestDto;
import com.logistics.company.application.dto.internal.response.UserExistsResponseDto;
import com.logistics.company.application.dto.internal.response.UserRoleUpdateResponseDto;
import com.logistics.company.application.port.UserPort;
import com.logistics.company.infrastructure.feign.client.UserClient;
import com.logistics.company.infrastructure.feign.request.UserRoleUpdateClientRequestDto;
import com.logistics.company.infrastructure.feign.response.UserExistsClientResponseDto;
import com.logistics.company.infrastructure.feign.response.UserRoleUpdateClientResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAdapter implements UserPort {

	private final UserClient userClient;

	@Override
	public UserRoleUpdateResponseDto companyManagerRoleUpdateRequest(UserRoleUpdateRequestDto request) {
		UserRoleUpdateClientResponseDto response = userClient.compnayManagerRoleUpdateRequest(
				toClientRequest(request)
		).getData();
		
		return toApplicationResponse(response);
	}

	@Override
	public UserExistsResponseDto userExistsRequest(Long userId) {
		UserExistsClientResponseDto response = userClient.userExistsRequest(userId).getData();
		
		return toApplicationResponse(response);
	}
	
	private UserRoleUpdateClientRequestDto toClientRequest(UserRoleUpdateRequestDto request) {
		return new UserRoleUpdateClientRequestDto(
				request.userId(),
				request.companyId(),
				request.hubId(),
				request.role()
		);
	}
	
	private UserRoleUpdateResponseDto toApplicationResponse(UserRoleUpdateClientResponseDto response) {
		return new UserRoleUpdateResponseDto(
				response.companyId(),
				response.hubId(),
				response.userId(),
				response.exists()
		);
	}
	
	private UserExistsResponseDto toApplicationResponse(UserExistsClientResponseDto response) {
		return new UserExistsResponseDto(
				response.userId(),
				response.exists()
		);
	}
	
}
