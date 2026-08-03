package com.logistics.hub.application.dto.query;

import org.springframework.data.domain.Pageable;

public record SearchHubQuery(String keyword, Pageable pageable) {
}
