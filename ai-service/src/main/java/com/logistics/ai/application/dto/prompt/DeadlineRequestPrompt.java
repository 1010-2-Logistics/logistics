package com.logistics.ai.application.dto.prompt;

import java.util.List;
import java.util.UUID;

import com.logistics.ai.application.dto.internal.ProductInfo;
import com.logistics.ai.application.event.OrderCreatedEvent;

public class DeadlineRequestPrompt {
	public static final String SYSTEM_PROMPT = """
			===[역할]===
			당신은 물류 배송 관리 전문 AI 입니다.
			전달받은 주문 데이터와 경로 정보, '배송담당자 근무시간: {배송담당자근무시간}'을 고려하여
			납기일자에 맞추기 위해 발송지에서 출발해야 하는 '최종 발송 시한'을
			계산하고 [담당자 안내 메시지]를 작성하세요.
			
			===[규칙]===
			1. 반드시 아래 [출력양식]을 완벽하게 준수하여 답변을 작성하세요.
			2. '최종 발송 시한'은 납기일, 이동 경로, 근무시간을 종합 고려하여
			차질 없이 도착할 수 있는 발송 시점을 산출하세요.
			3. 출력 양식 외의 인사말, 서론, 설명 등 부가적인 텍스트는 포함하지 마세요.
			4. '최종 발송 시한'은 100자 이내로 작성하세요.
			
			===[출력양식]===
			주문 번호 : {주문번호}
			주문자 정보 : {주문자이름} / {주문자이메일}
			주문 시간 : {주문시간}
			상품 정보 : {상품정보}
			요청 사항 : {요청사항}
			발송지 : {발송지}
			경유지 : {경유지}
			도착지 : {도착지}
			배송담당자 : {배송담당자이름} / {배송담당자이메일}
			
			최종 발송 시한 : {AI 계산한 일시 및 사유}
	""";
	
	private static final String USER_PROMPT_TEMPLATE = """
			다음 주문 정보를 바탕으로 [담당자 안내 메시지]를 작성하세요.
			
			- 주문번호: %s
			- 주문자이메일: %s
			- 주문시간: %s
			- 상품정보: %s
			- 요청사항: %s
			- 발송지: %s
			- 경유지: %s
			- 도착지: %s
			- 배송담당자이름: %s
			- 배송담당자이메일: %s
			- 배송담당자근무시간: 09:00 ~ 18:00
	""";
	
	public static String bindingPrompt(
			UUID orderId,
			String customerName,
			String customerEmail,
			String orderTime,
			String productInfo,
			String requestMessage,
			String startHubName,
			List<String> transitHubs,
			String endHubName,
			String deliveryManagerName,
			String deliveryManagerEmail
	) {
		
		String transitHubString = (transitHubs == null || transitHubs.isEmpty())
				? "경유지 없음"
				: String.join(", ", transitHubs);
		
		return String.format(USER_PROMPT_TEMPLATE,
				orderId.toString(),
				customerName,
				customerEmail,
				orderTime,
				productInfo,
				requestMessage,
				startHubName,
				transitHubString,
				endHubName,
				deliveryManagerName,
				deliveryManagerEmail
		);
	}
	
	// 파라미터 타입 미확정
	public static String bindingPrompt(OrderCreatedEvent event, ProductInfo product) {
		return null;
	}
	
}
