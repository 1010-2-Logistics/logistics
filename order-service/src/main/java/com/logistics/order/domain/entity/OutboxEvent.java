package com.logistics.order.domain.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "p_outbox_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {
    @Id
    private UUID eventId;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    private OutboxEvent(
            UUID eventId,
            UUID aggregateId,
            String eventType,
            String payload
    ) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public static OutboxEvent create(
            UUID aggregateId,
            String eventType,
            String payload
    ) {
        return new OutboxEvent(
                UUID.randomUUID(),
                aggregateId,
                eventType,
                payload
        );
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
}
