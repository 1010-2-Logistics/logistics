package com.logistics.ai.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "request_prompt", columnDefinition = "jsonb", nullable = false)
	private String requestPrompt;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "response_prompt", columnDefinition = "jsonb")
	private String responsePrompt;
	
	@Column(name = "final_deadline")
	private LocalDateTime finalDeadline;
	
	@Column(name = "ai_model")
	private String aiModel;
	
	@Column(name = "ai_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private AiStatus status;
	
	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;
	
	@Column(name = "time_ms")
	private Integer timeMs;
	
	@Column(name = "retry_count", nullable = false)
	private int retryCount;
	
	public static AiHistory create(UUID orderId, UUID deliveryId, String requestPrompt, String aiModel) {
		AiHistory aiHistory = new AiHistory();
		
		aiHistory.orderId = orderId;
		aiHistory.deliveryId = deliveryId;
		aiHistory.requestPrompt = requestPrompt;
		aiHistory.aiModel = aiModel;
		aiHistory.status = AiStatus.PENDING;
		aiHistory.retryCount = 0;
		
		return aiHistory;
	}
	
	public void success(String responsePrompt, LocalDateTime finalDeadline, int timeMs) {
		this.responsePrompt = responsePrompt;
		this.finalDeadline = finalDeadline;
		this.timeMs = timeMs;
		this.status = AiStatus.SUCCESS;
		this.errorMessage = null;
	}
	
	public void fail(String errorMessage, int timeMs) {
		this.errorMessage = errorMessage;
		this.status = AiStatus.FAILED;
		this.timeMs = timeMs;
	}
	
	public void increaseRetryCount() {
		this.retryCount++;
	}
	
}
