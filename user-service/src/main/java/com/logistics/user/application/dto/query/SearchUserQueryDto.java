package com.logistics.user.application.dto.query;

import org.springframework.data.domain.Pageable;

public record SearchUserQueryDto(String keyword, Pageable pageable) {
}
