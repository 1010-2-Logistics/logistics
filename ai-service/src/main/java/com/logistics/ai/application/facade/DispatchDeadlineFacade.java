package com.logistics.ai.application.facade;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.DeliveryManagerInfo;
import com.logistics.ai.application.dto.internal.HubInfo;
import com.logistics.ai.application.dto.internal.ProductInfo;
import com.logistics.ai.application.dto.internal.RouteInfo;
import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.port.in.DispatchDeadlineUseCase;
import com.logistics.ai.application.port.out.DeliveryPort;
import com.logistics.ai.application.port.out.HubPort;
import com.logistics.ai.application.port.out.ProductPort;
import com.logistics.ai.global.exception.AiErrorCode;
import com.logistics.ai.global.exception.AiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchDeadlineFacade implements DispatchDeadlineUseCase {
	
	private static final String DELIVERY_MANAGER_WORK_TIME = "09:00 ~ 18:00";
	
	private final DeliveryPort deliveryPort;
	
	private final ProductPort productPort;
	
	private final HubPort hubPort;

	@Override
	public void generate(OrderCreatedEvent event) {
		log.info("[AI-SERVICE]: OrderCreatedEvent 수신, orderId = {}, deliveryId = {}",
				event.orderId(),
				event.deliveryId()
		);
		
		// 경유 허브 목록 조회
		List<RouteInfo> routes = deliveryPort.getRoutes(event.deliveryId());
		
		// 경유 허브 목록의 허브 정보 조회
		Set<UUID> hubIds = routes.stream()
				.flatMap(route -> Stream.of(route.startHubId(), route.endHubId()))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		// 경유 허브 수 계산
		int hubWayPoint = Math.max(0, hubIds.size() -2);
		
		log.info("[AI-SERVICE]: 경유 허브 조회, totalHubCount = {}, hubPointCount = {}",
				hubIds.size(),
				hubWayPoint
		);
		
		// 상품 정보 조회
		ProductInfo product = productPort.getProduct(event.productId());
		
		log.info("[AI-SERVICE]: 상품 조회, productId = {}",
				product.productId()
		);
		
		List<HubInfo> hubInfoList = hubPort.getHubInfo(hubIds);
		
		Map<UUID, HubInfo> hubMap = hubInfoList.stream()
				.collect(Collectors.toMap(HubInfo::hubId, hub -> hub));
		
		validateHubIdsMatch(hubMap, hubIds);
		
		// 첫 번째 허브 배송 기사 정보 조회
		DeliveryManagerInfo deliveryManagerInfo = new DeliveryManagerInfo(
				"임시 배송자",
				"임시 배송 슬랙아이디"
		);
		
		
		
		// === 가져온 정보들로 조합 === //
		// AI 에게 보낼 메세지 //
		// ============================ //
		
		
		
		
		// === DispatchDeadlineGenerationPort === //
		// 제미나이 호출 //
		//
		
	}
	
	private void validateHubIdsMatch(Map<UUID, HubInfo> hubMap, Set<UUID> hubIds) {
		if(!hubMap.keySet().containsAll(hubIds)) {
			Set<UUID> misMatchHubIds = new HashSet<>(hubIds);
			misMatchHubIds.removeAll(hubMap.keySet());
			
			log.error("[AI-SERVICE]: 허브 정보 정합성 오류, misMatchHubIds = {}",
					misMatchHubIds
			);
			
			throw new AiException(AiErrorCode.AI_HUB_INFO_INCOMPLETE);
		}
	}
	
	private String getTransitHubNames(Map<UUID, HubInfo> hubMap, List<RouteInfo> routes) {
		StringBuilder hubNames = new StringBuilder();
		
		for (int i = 0; i < routes.size() - 1; i++) {
			UUID transitHubId = routes.get(i).endHubId();
			HubInfo transitHub = hubMap.get(transitHubId);
			
			if(transitHub != null) {
				hubNames.append(transitHub.hubName() + ", ");
			}
			
		}
		
		if(hubNames.length() > 0) {
			hubNames.setLength(hubNames.length() - 2);
		}
		
		return hubNames.toString();
	}
	
	private String getHubName(Map<UUID, HubInfo> hubMap, UUID hubId, String defaultMessage) {
		if (hubId == null) return defaultMessage;
		
		HubInfo hub = hubMap.get(hubId);
		
		return (hub != null)
				? hub.hubName()
				: String.format("%s 정보 없음(서버 에러)", defaultMessage);
	}
	
}
