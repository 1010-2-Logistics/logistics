package com.logistics.ai.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_ai")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiHistory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "ai_id")
	private UUID aiId;
	
	@Column(name = "order_id", nullable = false)
	private UUID orderId;
	
	@Column(name = "delivery_id", nullable = false)
	private UUID deliveryId;
	
	@Column(name = "request_prompt", columnDefinition = "TEXT", nullable = false)
	private String requestPrompt;
	
	@Column(name = "response_prompt", columnDefinition = "TEXT")
	private String responsePrompt;
	
	@Column(name = "final_deadline")
	private LocalDateTime finalDeadline;
	
	@Column(name = "ai_model")
	private String aiModel;
	
	@Column(name = "ai_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private AiStatus status;
	
	@Column(name = "call_message", columnDefinition = "TEXT")
	private String callMessage;
	
	@Column(name = "time_ms")
	private Integer timeMs;
	
	@Column(name = "retry_count", nullable = false)
	private int retryCount;
	
	public static AiHistory succeded(UUID orderId, UUID deliveryId, String requestPrompt, String aiModel) {
		AiHistory aiHistory = new AiHistory();
		
		aiHistory.orderId = orderId;
		aiHistory.deliveryId = deliveryId;
		aiHistory.requestPrompt = requestPrompt;
		aiHistory.aiModel = aiModel;
		aiHistory.status = AiStatus.PENDING;
		aiHistory.retryCount = 0;
		
		return aiHistory;
	}
	
	public static AiHistory failed(UUID orderId, UUID deliveryId, String requestPrompt, String aiModel, String callMessage, Integer retryCount) {
		AiHistory aiHistory = new AiHistory();
		
		aiHistory.orderId = orderId;
		aiHistory.deliveryId = deliveryId;
		aiHistory.requestPrompt = requestPrompt;
		aiHistory.aiModel = aiModel;
		aiHistory.callMessage = callMessage;
		aiHistory.status = AiStatus.FAILED;
		aiHistory.retryCount = retryCount;
		
		return aiHistory;
	}
	
	public void success(String responsePrompt, LocalDateTime finalDeadline, int timeMs, int retryCount, String callMessage) {
		this.responsePrompt = responsePrompt;
		this.finalDeadline = finalDeadline;
		this.timeMs = timeMs;
		this.retryCount = retryCount;
		this.callMessage = callMessage;
		this.status = AiStatus.SUCCESS;
	}
	
	public void increaseRetryCount() {
		this.retryCount++;
	}
	
}
