package com.logistics.ai.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.ai.application.dto.result.DlqRedriveResult;
import com.logistics.ai.application.port.in.OrderCreatedRedrive;
import com.logistics.ai.domain.entity.Role;
import com.logistics.ai.global.exception.AiErrorCode;
import com.logistics.ai.global.exception.AiException;
import com.logistics.ai.global.exception.CommonErrorCode;
import com.logistics.ai.global.response.ApiResponse;
import com.logistics.ai.infrastructure.security.principal.UserPrincipal;
import com.logistics.ai.presentation.interceptor.NoAuthentication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "AI")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class OrderCreatedRedriveController {

	private final OrderCreatedRedrive redriver;
	
	@Operation(
			summary = "주문 생성 메시지 재처리",
			description = """
					접근 권한:
					  - 관리자		
			"""
	)
	@PostMapping("/redrive")
	@NoAuthentication
	public ApiResponse<DlqRedriveResult> redrive(
			@RequestParam(name = "count", defaultValue = "1") int count,
			@AuthenticationPrincipal UserPrincipal user) {
		
		if(user == null || user.getRole() != Role.MASTER) {
			throw new AiException(CommonErrorCode.AUTH_FORBIDDEN);
		}
		
		if(count < 1 || count > 10) {
			throw new AiException(AiErrorCode.AI_DLQ_REDRIVE_COUNT_INVALID);
		}
		
		DlqRedriveResult result = redriver.redrive(count);
		
		return ApiResponse.success(
				HttpStatus.OK.value(),
				"DLQ 메시지 원본 재전달 완료",
				result
		);
	}
}
