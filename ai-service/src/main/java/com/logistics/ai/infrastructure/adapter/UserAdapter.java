package com.logistics.ai.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.UserInfo;
import com.logistics.ai.application.port.out.UserPort;
import com.logistics.ai.infrastructure.exception.NonRetryRemoteException;
import com.logistics.ai.infrastructure.exception.RemoteErrorCode;
import com.logistics.ai.infrastructure.exception.RetryRemoteException;
import com.logistics.ai.infrastructure.feign.client.UserClient;
import com.logistics.ai.infrastructure.feign.response.UserInfoResponseDto;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAdapter implements UserPort {

	private final UserClient userClient;

	@Override
	public UserInfo getUserInfo(Long userId) {
		try {
			UserInfoResponseDto response = userClient.getUserInfo(userId).getData();
			
			return UserAdapter.from(response);
		}
		
		catch (RetryableException e) {
			log.error(
					"[AI-SERVICE]: User Service 통신 장애. status = {}, message = {}",
					e.status(),
					e.getMessage()
			);
			throw new RetryRemoteException(
					RemoteErrorCode.USER_REMOTE_ERROR,
					e.getMessage()
			);
		}
		
		catch (FeignException.NotFound e) {
			log.warn(
					"[AI-SERVICE]: 대상 회원 정보를 찾을 수 없습니다. userId = {}",
					userId
			);
			throw new NonRetryRemoteException(RemoteErrorCode.USER_NOT_FOUND);
		}
		
		catch (FeignException e) {
			if(e.status() >= 500) {
				throw new RetryRemoteException(
						RemoteErrorCode.USER_REMOTE_ERROR,
						e.getMessage()
				);
			}
			
			throw new NonRetryRemoteException(RemoteErrorCode.USER_NOT_FOUND);
		}
	}
	
	private static UserInfo from(UserInfoResponseDto response) {
		return new UserInfo(
				response.userId(),
				response.name(),
				response.slackId()
		);
	}
	
	
}
