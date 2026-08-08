package com.logistics.slack.infrastructure.persistence.repository;

import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.entity.SlackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

interface SlackJpaRepository extends JpaRepository<Slack, UUID> {
    Optional<Slack> findBySlackMessageIdAndDeletedAtIsNull(
            UUID slackMessageId
    );


    @Query("""
            SELECT s
            FROM Slack s
            WHERE s.deletedAt IS NULL
            AND (:status IS NULL OR s.status = :status)
            AND (:senderId IS NULL OR s.senderId = :senderId)
            AND (:receiverId IS NULL OR s.receiverId = :receiverId)
            AND (:referenceId IS NULL OR s.referenceId = :referenceId)
            AND (cast(:createdFrom as LocalDateTime) IS NULL OR s.createdAt >= :createdFrom)
            AND (cast(:createdTo as LocalDateTime) IS NULL OR s.createdAt <= :createdTo)
            """)
    Page<Slack> search(
            @Param("status") SlackStatus status,
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("referenceId") UUID referenceId,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            Pageable pageable
    );
}
