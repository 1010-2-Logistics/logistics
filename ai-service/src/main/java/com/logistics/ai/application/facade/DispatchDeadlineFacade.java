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
import com.logistics.ai.application.dto.result.DispatchDeadlineRetryResultDto;
import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.port.in.DeadlineGenerationRetryService;
import com.logistics.ai.application.port.in.DispatchDeadlineCommandService;
import com.logistics.ai.application.port.in.DispatchDeadlineUseCase;
import com.logistics.ai.application.service.DispatchDeadlineQueryService;
import com.logistics.ai.application.port.out.DeliveryPort;
import com.logistics.ai.application.port.out.HubPort;
import com.logistics.ai.application.port.out.ProductPort;
import com.logistics.ai.application.util.DeadlinePromptSupport;
import com.logistics.ai.domain.entity.AiHistory;
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
	private final DispatchDeadlineQueryService queryService;

	@Override
	public void generate(OrderCreatedEvent event) {
		if(queryService.hasSucceeded(event.orderId())) {
			return;
		}
//		// 테스트코드
//		UUID startHubId = UUID.randomUUID();
//		UUID hubId2 = UUID.randomUUID();
//		UUID endHubId = UUID.randomUUID();
//		
//		UUID productId = UUID.randomUUID();
//		
//		// 경유 허브 목록 조회
//		List<RouteInfo> routes = List.of(
//				new RouteInfo(0, startHubId, hubId2, 1L, BigDecimal.valueOf(20), 60),
//				new RouteInfo(0, hubId2, endHubId, 2L, BigDecimal.valueOf(30), 60)
//		);
//		
//		if(routes == null || routes.isEmpty()) {
//			throw new AiException(AiErrorCode.AI_DELIVERY_ROUTE_EMPTY);
//		}
//		
//		// 경유 허브 목록의 허브 정보 조회
//		Set<UUID> hubIds = routes.stream()
//				.flatMap(route -> Stream.of(route.startHubId(), route.endHubId()))
//				.filter(Objects::nonNull)
//				.collect(Collectors.toSet());
//		
//		// 경유 허브 수 계산
//		int hubWayPoint = Math.max(0, hubIds.size() - 2);
//		
//		log.info("[AI-SERVICE]: 경유 허브 조회, totalHubCount = {}, hubPointCount = {}",
//				hubIds.size(),
//				hubWayPoint
//		);
//		
//		// 상품 정보 조회
//		//ProductInfo product = productPort.getProduct(event.productId());
//		ProductInfo product = new ProductInfo(
//				productId,
//				"제로 콜라 2L",
//				"(주) 코카콜라"
//		);
//		
//		log.info("[AI-SERVICE]: 상품 조회, productId = {}",
//				product.productId()
//		);
//		
//		// List<HubInfo> hubInfoList = hubPort.getHubInfo(hubIds);
//		List<HubInfo> hubInfoList = List.of(
//				new HubInfo(startHubId, "서울 허브", "서울 특별시"),
//				new HubInfo(hubId2, "대전 허브", "대전 광역시"),
//				new HubInfo(endHubId, "대구 허브", "대구 광역시")
//		);
//		
//		Map<UUID, HubInfo> hubMap = hubInfoList.stream()
//				.collect(Collectors.toMap(HubInfo::hubId, hub -> hub));
//		
//		validateHubIdsMatch(hubMap, hubIds);
//		
//		// 첫 번째 허브 배송 기사 정보 조회
//		DeliveryManagerInfo deliveryManagerInfo = new DeliveryManagerInfo(
//				"배달킹",
//				"U230132"
//		);
//		
//		// AI 요청 프롬프트 생성
//		String requestPrompt = DeadlinePromptSupport.generatedPrompt(
//				event,
//				product,
//				deliveryManagerInfo,
//				hubMap,
//				routes,
//				hubWayPoint
//		);
//		
//		// 제미나이 호출 //
//		String aiModel = DeadlinePromptSupport.aiModelSelector(hubWayPoint);
//		
//		DispatchDeadlineRetryResultDto result = deadlineGenerationRetryService.generate(requestPrompt, aiModel);
//		
//		AiHistory successHistory = AiHistory.succeded(
//				event.orderId(),
//				event.deliveryId(),
//				requestPrompt,
//				aiModel
//		);
//		
//		successHistory.success(
//				result.responsePrompt(),
//				result.finalDeadline(),
//				result.timeMs(),
//				result.retryCount(),
//				result.lastRetryReason()
//		);
//		
//		commandService.saveSucceeded(successHistory);
		
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
		
		// AI 요청 프롬프트 생성
		String requestPrompt = DeadlinePromptSupport.generatedPrompt(
				event,
				product,
				deliveryManagerInfo,
				hubMap,
				routes,
				hubWayPoint
				);
		
		// 제미나이 호출 //
		String aiModel = DeadlinePromptSupport.aiModelSelector(hubWayPoint);
		
		// AI API 호출부
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
		
		commandService.saveSucceeded(successHistory);
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
