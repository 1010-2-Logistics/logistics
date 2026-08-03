package com.logistics.template.application.dto.query;

import org.springframework.data.domain.Pageable;

public record SearchInventoryQuery(String keyword, Pageable pageable) {
}
