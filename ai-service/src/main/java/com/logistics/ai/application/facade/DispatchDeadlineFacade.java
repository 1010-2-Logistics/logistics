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

import com.logistics.ai.application.dto.internal.HubInfo;
import com.logistics.ai.application.dto.internal.ProductInfo;
import com.logistics.ai.application.dto.internal.RouteInfo;
import com.logistics.ai.application.dto.internal.UserInfo;
import com.logistics.ai.application.dto.result.DispatchDeadlineRetryResultDto;
import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.event.SlackEvent;
import com.logistics.ai.application.port.in.DeadlineGenerationRetryService;
import com.logistics.ai.application.port.in.DispatchDeadlineCommandService;
import com.logistics.ai.application.port.in.DispatchDeadlineUseCase;
import com.logistics.ai.application.port.out.DeliveryPort;
import com.logistics.ai.application.port.out.HubPort;
import com.logistics.ai.application.port.out.ProductPort;
import com.logistics.ai.application.port.out.SlackEventPublisher;
import com.logistics.ai.application.port.out.UserPort;
import com.logistics.ai.application.util.DeadlinePromptSupport;
import com.logistics.ai.domain.entity.AiHistory;
import com.logistics.ai.domain.entity.AiStatus;
import com.logistics.ai.global.exception.AiErrorCode;
import com.logistics.ai.global.exception.AiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchDeadlineFacade implements DispatchDeadlineUseCase {
	
	private final DispatchDeadlineCommandService commandService;
	
	private final DeadlineGenerationRetryService deadlineGenerationRetryService;
	
	private final DeliveryPort deliveryPort;
	
	private final ProductPort productPort;
	
	private final HubPort hubPort;

	private final UserPort userPort;
	
	private final SlackEventPublisher slackPublisher;
	
	@Override
	public void generate(OrderCreatedEvent event) {
		// 원본코드
		// 경유 허브 목록 조회
		List<RouteInfo> routes = deliveryPort.getRoutes(event.deliveryId());
		
		if(routes == null || routes.isEmpty()) {
			throw new AiException(AiErrorCode.AI_DELIVERY_ROUTE_EMPTY);
		}
		
		// 경유 허브 목록의 허브 정보 조회
		Set<UUID> hubIds = routes.stream()
				.flatMap(route -> Stream.of(route.startHubId(), route.endHubId()))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		// 경유 허브 수 계산
		int hubWayPoint = Math.max(0, hubIds.size() -2);
		
		log.info("[AI-SERVICE]: 경유 허브 조회, totalHubCount = {}, hubWayPointCount = {}",
				hubIds.size(),
				hubWayPoint
				);
		
		// 내부 API 호출 2 - 상품 정보 조회
		ProductInfo product = productPort.getProduct(event.productId());
		
		log.info("[AI-SERVICE]: 상품 조회, productId = {}",
				product.productId()
				);
		
		// 내부 API 호출 3 - 허브 정보 조회
		List<HubInfo> hubInfoList = hubPort.getHubInfo(hubIds);
		
		// 허브 정보와 불러온 허브 상세 정보를 매칭
		Map<UUID, HubInfo> hubMap = hubInfoList.stream()
				.collect(Collectors.toMap(HubInfo::hubId, hub -> hub));
		
		// 추출 허브 정보와 불러온 허브끼리의 정합성 검사
		validateHubIdsMatch(hubMap, hubIds);
		
		// 출발 허브의 배송 기사 정보 조회
		UserInfo deliveryManagerInfo = userPort.getUserInfo(routes.get(0).startDeliveryManagerId());
		
		// AI 요청 프롬프트 생성
		String requestPrompt = DeadlinePromptSupport.generatedPrompt(
				event,
				product,
				deliveryManagerInfo,
				hubMap,
				routes,
				hubWayPoint
				);
		
		// 제미나이 호출
		String aiModel = DeadlinePromptSupport.aiModelSelector(hubWayPoint);
		
		DispatchDeadlineRetryResultDto result = deadlineGenerationRetryService.generate(requestPrompt, aiModel);
		
		AiHistory successHistory = AiHistory.succeded(
				event.orderId(),
				event.deliveryId(),
				requestPrompt,
				aiModel
				);
		
		successHistory.success(
				result.responsePrompt(),
				result.finalDeadline(),
				result.timeMs(),
				result.retryCount(),
				result.lastRetryReason()
				);
		
		AiHistory aiHistory = commandService.saveSucceeded(successHistory);
		
		if(aiHistory.getStatus() == AiStatus.SUCCESS) {
			// 이벤트 발행
			slackPublisher.publish(new SlackEvent(
					deliveryManagerInfo.userId(),
					result.responsePrompt(),
					aiHistory.getAiId()
			));
		}
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
	
}
