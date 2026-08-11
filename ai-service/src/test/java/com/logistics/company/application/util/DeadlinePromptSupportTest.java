package com.logistics.company.application.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.logistics.ai.application.dto.internal.HubInfo;
import com.logistics.ai.application.dto.internal.ProductInfo;
import com.logistics.ai.application.dto.internal.RouteInfo;
import com.logistics.ai.application.dto.internal.UserInfo;
import com.logistics.ai.application.event.OrderCreatedEvent;
import com.logistics.ai.application.util.DeadlinePromptSupport;

public class DeadlinePromptSupportTest {

	@Test
	@DisplayName("주문과 배송 정보를 이용하여 프롬프트 생성")
	void generatedPrompt_success() {
		UUID orderId = UUID.randomUUID();
		UUID deliveryId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();
		
		UUID startHubId = UUID.randomUUID();
		UUID endHubId = UUID.randomUUID();
		
		OrderCreatedEvent event = new OrderCreatedEvent(
				orderId, deliveryId, productId,
				5,
				"최대한 빨리 보내주세요.",
				"주문자 이름",
				"주문자 슬랙 아이디",
				LocalDateTime.of(2026, 8, 10, 14,30)
		);
		
		ProductInfo product = new ProductInfo(
				productId,
				"컴퓨터",
				"스파르타 컴퓨터"
		);
		
		UserInfo deliveryManager = new UserInfo(
				1L,
				"배송담당자",
				"U018234"
		);
		
		List<RouteInfo> routes = List.of(
				new RouteInfo(0, startHubId, endHubId, 1L, BigDecimal.valueOf(20), 60)
		);
		
		Map<UUID, HubInfo> hubMap = Map.of(
				startHubId,
				new HubInfo(startHubId, "서울 허브", "서울특별시"),
				
				endHubId,
				new HubInfo(endHubId, "부산 허브", "부산특별시")
		);
		
		String prompt = DeadlinePromptSupport.generatedPrompt(
				event,
				product,
				deliveryManager,
				hubMap,
				routes,
				0
		);
		
		assertThat(prompt).contains(
				orderId.toString(),
				event.receiverName(),
				event.receiverSlackId(),
				event.request(),
				product.productName() + " " + event.quantity() + " EA",
				hubMap.get(startHubId).name(),
				hubMap.get(endHubId).name(),
				deliveryManager.name(),
				deliveryManager.slackId()
		);
	}
	
	@Test
	@DisplayName("주문과 배송 정보를 이용하여 프롬프트 생성 - 경유 허브 여러개")
	void generatedPrompt_success_multiHub() {
		UUID orderId = UUID.randomUUID();
		UUID deliveryId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();
		
		UUID finalStartHubId = UUID.randomUUID();
		UUID hubId2 = UUID.randomUUID();
		UUID hubId3 = UUID.randomUUID();
		UUID hubId4 = UUID.randomUUID();
		UUID hubId5 = UUID.randomUUID();
		UUID finalEndHubId = UUID.randomUUID();
		
		OrderCreatedEvent event = new OrderCreatedEvent(
				orderId, deliveryId, productId,
				5,
				"최대한 빨리 보내주세요.",
				"주문자 이름",
				"주문자 슬랙 아이디",
				LocalDateTime.of(2026, 8, 10, 14,30)
				);
		
		ProductInfo product = new ProductInfo(
				productId,
				"컴퓨터",
				"스파르타 컴퓨터"
				);
		
		UserInfo deliveryManager = new UserInfo(
				1L,
				"배송담당자",
				"U018234"
				);
		
		List<RouteInfo> routes = List.of(
				new RouteInfo(0, finalStartHubId, hubId2, 1L, BigDecimal.valueOf(20), 60),
				new RouteInfo(1, hubId2, hubId3, 2L, BigDecimal.valueOf(30), 60),
				new RouteInfo(2, hubId3, hubId4, 3L, BigDecimal.valueOf(40), 60),
				new RouteInfo(3, hubId4, hubId5, 4L, BigDecimal.valueOf(50), 60),
				new RouteInfo(4, hubId5, finalEndHubId, 5L, BigDecimal.valueOf(60), 60)
		);
		
		Map<UUID, HubInfo> hubMap = Map.of(
				finalStartHubId,
				new HubInfo(finalStartHubId, "서울 허브", "서울특별시"),
				
				hubId2,
				new HubInfo(hubId2, "인천 허브", "인천광역시"),
				
				hubId3,
				new HubInfo(hubId3, "대전 허브", "대전광역시"),
				
				hubId4,
				new HubInfo(hubId4, "대구 허브", "대구광역시"),
				
				hubId5,
				new HubInfo(hubId5, "창원 허브", "창원특례시"),
				
				finalEndHubId,
				new HubInfo(finalEndHubId, "부산 허브", "부산광역시")
		);
		
		String prompt = DeadlinePromptSupport.generatedPrompt(
				event,
				product,
				deliveryManager,
				hubMap,
				routes,
				routes.size() - 2
		);
		
		assertThat(prompt).contains(
				orderId.toString(),
				event.receiverName(),
				event.receiverSlackId(),
				event.request(),
				product.productName() + " " + event.quantity() + " EA",
				hubMap.get(finalStartHubId).name(),
				hubMap.get(finalEndHubId).name(),
				deliveryManager.name(),
				deliveryManager.slackId(),
				
				// 경유지
				"인천 허브, 대전 허브, 대구 허브, 창원 허브"
		);
	}
	
	@Test
	@DisplayName("경유 허브가 3개 이상이면 pro 모델 선택")
	void aiModel_pro() {
		assertThat(DeadlinePromptSupport.aiModelSelector(3)).isEqualTo("gemini-1.5-pro");
	}
	
	@Test
	@DisplayName("경유 허브가 3개 미만이면 flash 모델 선택")
	void aiModel_flash() {
		assertThat(DeadlinePromptSupport.aiModelSelector(0)).isEqualTo("gemini-1.5-flash");
		
		assertThat(DeadlinePromptSupport.aiModelSelector(1)).isEqualTo("gemini-1.5-flash");
		
		assertThat(DeadlinePromptSupport.aiModelSelector(2)).isEqualTo("gemini-1.5-flash");
	}
	
	
	
}
