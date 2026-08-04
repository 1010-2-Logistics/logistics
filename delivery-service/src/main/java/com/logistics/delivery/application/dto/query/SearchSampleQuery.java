package com.logistics.delivery.application.dto.query;

import org.springframework.data.domain.Pageable;

public record SearchSampleQuery(String keyword, Pageable pageable) {
}
