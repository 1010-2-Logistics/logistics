package com.logistics.hubRoute.application.dto.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record SearchHubRouteQuery(UUID hubRouteId, Pageable pageable) {
}
