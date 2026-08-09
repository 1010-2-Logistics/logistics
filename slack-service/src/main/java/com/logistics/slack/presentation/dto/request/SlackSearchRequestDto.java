package com.logistics.slack.presentation.dto.request;

import com.logistics.slack.domain.entity.SlackStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlackSearchRequestDto(
        SlackStatus status,
        Long senderId,
        Long receiverId,
        UUID referenceId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdTo,

        String sort,
        Integer page,
        Integer size
) {
}
