package com.logistics.slack.domain.entity;

import com.logistics.slack.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_slack")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Slack extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slack_message_id")
    private UUID slackMessageId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId; // 발신자 식별자

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId; // 수신자 식별자

    @Column(name = "message", nullable = false)
    private String message; // 메시지 내용

    @Column(name = "error_message")
    private String errorMessage; // 오류 메시지

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount; // 재시도 횟수

    @Column(name = "sent_at")
    private LocalDateTime sentAt; // 실제 발송 완료 시간

    @Column(name = "reference_id")
    private Long referenceId; // 관련 업무 식별자 (해당 주문·배송과 관련해 어떤 슬랙 메시지를 보냈는지)

//    // ERD에는 있고 테이블 명세서에는 없다 -> 근데 sendAt 같음
//    @Column(name = "send_time", nullable = false)
//    private Instant send_time;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlackStatus status; // 발송 상태

    public static Slack create(
            Long senderId,
            Long receiverId,
            String message,
            Long referenceId
    ) {
        Slack slack = new Slack();
        slack.senderId = senderId;
        slack.receiverId = receiverId;
        slack.message = message;
        slack.referenceId = referenceId;
        slack.retryCount = 0;
        slack.status = SlackStatus.PENDING;

        return slack;
    }

    public void markSuccess() {
        this.status = SlackStatus.SUCCESS;
        this.sentAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = SlackStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
