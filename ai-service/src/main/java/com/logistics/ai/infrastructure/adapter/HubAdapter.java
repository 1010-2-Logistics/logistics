package com.logistics.ai.infrastructure.adapter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.HubInfo;
import com.logistics.ai.application.port.out.HubPort;
import com.logistics.ai.infrastructure.exception.NonRetryRemoteException;
import com.logistics.ai.infrastructure.exception.RemoteErrorCode;
import com.logistics.ai.infrastructure.exception.RetryRemoteException;
import com.logistics.ai.infrastructure.feign.client.HubClient;
import com.logistics.ai.infrastructure.feign.response.HubInfoResponseDto;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubAdapter implements HubPort {

	private final HubClient hubClient;

	@Override
	public List<HubInfo> getHubInfo(Set<UUID> hubIds) {
		try {
			Set<HubInfoResponseDto> hubInfoList = hubClient.getHubInfoList(List.copyOf(hubIds));
			
			return hubInfoList.stream()
					.map(HubInfoResponseDto::toApplication)
					.toList();
		}
		
		catch (RetryableException e) {
			log.error(
					"[AI-SERVICE]: Hub Service 통신 장애. status = {}, message = {}",
					e.status(),
					e.getMessage()
			);
			throw new RetryRemoteException(
					RemoteErrorCode.HUB_REMOTE_ERROR,
					e.getMessage()
			);
		}
		
		catch (FeignException.NotFound e) {
			log.warn(
					"[AI-SERVICE]: 허브 정보를 찾을 수 없습니다. hubId = {}",
					hubIds
			);
			
			throw new NonRetryRemoteException(RemoteErrorCode.HUB_INFO_NOT_FOUND);
		}
		
		catch (FeignException e) {
			if(e.status() >= 500) {
				throw new RetryRemoteException(
						RemoteErrorCode.HUB_REMOTE_ERROR,
						e.getMessage()
				);
			}
			
			throw new NonRetryRemoteException(RemoteErrorCode.HUB_REMOTE_ERROR);
		}
	}
}
