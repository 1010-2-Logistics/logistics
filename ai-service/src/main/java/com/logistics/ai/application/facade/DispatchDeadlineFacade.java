package com.logistics.ai.application.facade;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.logistics.ai.application.dto.internal.HubInfo;
import com.logistics.ai.application.dto.internal.ProductInfo;
import com.logistics.ai.application.dto.internal.RouteInfo;
import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.port.in.DispatchDeadlineUseCase;
import com.logistics.ai.application.port.out.DeliveryPort;
import com.logistics.ai.application.port.out.HubPort;
import com.logistics.ai.application.port.out.ProductPort;

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
		
		// === DeliveryPort === //
		// 2. event.deliveryId() 로 배송정보 조회
		// startHubId, endHubId, deliveryAddress, 배송 담당자 ID
		// /internal/v1/deliveries/{deliveryId}
		// ??? delivery 는 같은 데이터베이스라서 배송정보 조회할 때
		// 경유하는 허브 목록 같이 조회하면 좋을듯
		
		
		// === DeliveryRoutPort === // :내부 API 미구현
		// 3. event.deliveryId() 로 경유 허브 목록 조회
		// 경유 허브 목록 리스트에서
		// DeliveryRoute.startHubId가 DeliveryPort에서 받아온 startHubId와 같은걸 1번
		// 그 1번의 endHubId가 다음 startHubId 인걸로 2번 ... 정렬해야 함.
		// ErrorDecoder
		List<RouteInfo> routes = deliveryPort.getRoutes(event.deliveryId());
		
		Set<UUID> hubIds = routes.stream()
				.flatMap(route -> Stream.of(route.startHubId(), route.endHubId()))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		int hubWayPoint = Math.max(0, hubIds.size() -2);
		
		log.info("[AI-SERVICE]: 경유 허브 조회, totalHubCount = {}, hubPointCount = {}",
				hubIds.size(),
				hubWayPoint
		);
		
		// === ProductPort === //
		// 4. OrderPort에서 받아온 productId 를 통해 상품 정보 조회
		// /internal/v1/products/{productId}
		ProductInfo product = productPort.getProduct(event.productId());
		
		log.info("[AI-SERVICE]: 상품 조회, productId = {}",
				product.productId()
		);
		
		// === HubPort === // :내부 API 미구현
		// 5. Set<UUID> 해서 각 경유 hubId 의 주소를 가져온다.
		// /internal/v1/hubs?=hub_uuid_1,hub_uuid_2,hub_uuid_3,hub_uuid_4,hub_uuid_5
		List<HubInfo> hubInfoList = hubPort.getHubInfo(hubIds);
		
		// === 가져온 정보들로 조합 === //
		// AI 에게 보낼 메세지 //
		// ============================ //
		
		
		
		
		// === DispatchDeadlineGenerationPort === //
		// 제미나이 호출 //
		//
		
	}
	
}
