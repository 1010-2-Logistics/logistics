package com.logistics.hub.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record SearchHubQuery(UUID hubId, Pageable pageable) {
}
