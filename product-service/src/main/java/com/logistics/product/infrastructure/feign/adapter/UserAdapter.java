package com.logistics.product.infrastructure.feign.adapter;

import org.springframework.stereotype.Component;

import com.logistics.product.application.dto.internal.response.UserExistsResponseDto;
import com.logistics.product.application.port.UserPort;
import com.logistics.product.infrastructure.feign.client.UserClient;
import com.logistics.product.infrastructure.feign.response.UserExistsClientResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAdapter implements UserPort {

	private final UserClient userClient;

	@Override
	public UserExistsResponseDto getUserExists(Long userId) {
		UserExistsClientResponseDto response = userClient.getUserAuthentication(userId).getData();
		
		return new UserExistsResponseDto(
				response.userId(),
				response.role(),
				response.companyId(),
				response.hubId()
		);
	}
	
	
}
