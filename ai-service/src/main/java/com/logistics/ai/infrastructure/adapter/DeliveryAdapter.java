package com.logistics.ai.infrastructure.adapter;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.RouteInfo;
import com.logistics.ai.application.port.out.DeliveryPort;
import com.logistics.ai.infrastructure.exception.NonRetryRemoteException;
import com.logistics.ai.infrastructure.exception.RemoteErrorCode;
import com.logistics.ai.infrastructure.exception.RetryRemoteException;
import com.logistics.ai.infrastructure.feign.client.DeliveryClient;
import com.logistics.ai.infrastructure.feign.response.DeliveryRouteListResponseDto;
import com.logistics.ai.infrastructure.feign.response.DeliveryRouteListResponseDto.RouteItem;

import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryAdapter implements DeliveryPort {

	private final DeliveryClient deliveryClient;

	@Override
	public List<RouteInfo> getRoutes(UUID deliveryId) {
		try {
			DeliveryRouteListResponseDto response = deliveryClient.getRoutes(deliveryId).getData();
			
			List<RouteItem> routes = response.routes();
			
			return routes.stream()
					.sorted(Comparator.comparing(RouteItem::sequence, Comparator.nullsLast(Comparator.naturalOrder())))
					.map(RouteItem::toApplication)
					.toList();
		}
		
		catch (RetryableException e) {
			log.error(
					"[AI-SERVICE]: Delivery Service 통신 장애. status = {}, message = {}",
					e.status(),
					e.getMessage()
			);
			throw new RetryRemoteException(
					RemoteErrorCode.DELIVERY_REMOTE_ERROR,
					e.getMessage()
			);
		}
		
		catch (FeignException.NotFound e) {
			log.warn(
					"[AI-SERVICE]: 배송 정보를 찾을 수 없습니다. deliveryId = {}",
					deliveryId
			);
			throw new NonRetryRemoteException(RemoteErrorCode.DELIVERY_HUB_ROUTE_NOT_FOUND);
		}
		
		catch (FeignException e) {
			if(e.status() >= 500) {
				throw new RetryRemoteException(
						RemoteErrorCode.DELIVERY_REMOTE_ERROR,
						e.getMessage()
				);
			}
			
			throw new NonRetryRemoteException(RemoteErrorCode.DELIVERY_HUB_ROUTE_NOT_FOUND);
		}
		
	}
}
