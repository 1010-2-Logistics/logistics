package com.logistics.slack.application.dto.query;

import com.logistics.slack.domain.entity.SlackStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackSearchQuery(
        SlackStatus status,
        Long senderId,
        Long receiverId,
        Long referenceId,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        Pageable pageable

) {
    public static SlackSearchQuery of(
            SlackStatus status,
            Long senderId,
            Long receiverId,
            Long referenceId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        return new SlackSearchQuery(
                status,
                senderId,
                receiverId,
                referenceId,
                createdFrom,
                createdTo,
                pageable
        );
    }
}
