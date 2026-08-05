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
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 실제 서비스로 복사할 때: Sample -> 도메인 엔티티명, p_sample -> p_{테이블명}으로 바꾸세요.
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
    private String senderId; // 발신자 식별자

    @Column(name = "receiver_id", nullable = false)
    private String receiverId; // 수신자 식별자

    @Column(name = "message", nullable = false)
    private String message; // 메시지 내용

    @Column(name = "error_message")
    private String errorMessage; // 오류 메시지

    @Column(name = "retry_count", nullable = false)
    private String retryCount; // 재시도 횟수

    @Column(name = "sent_at")
    private String sentAt; // 실제 발송 완료 시간

    @Column(name = "reference_id")
    private String referenceId; // 관련 업무 식별자

    // ERD에는 있고 테이블 명세서에는 없다
    @Column(name = "send_time", nullable = false)
    private Instant send_time;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlackStatus status; // 발송 상태

    public static Slack create(
            String receiverId,
            String message,
            String referenceId
    ) {
        Slack slack = new Slack();
        slack.receiverId = receiverId;
        slack.message = message;
        slack.referenceId = referenceId;
        slack.status = SlackStatus.PENDING;
        return slack;
    }

//    public void update(String name) {
//        this.name = name;
//    }

    public void changeStatus(SlackStatus status) {
        this.status = status;
    }
}
