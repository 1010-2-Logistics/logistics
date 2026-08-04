package com.logistics.user.service.dto.query;

import org.springframework.data.domain.Pageable;

public record SearchUserQuery(String keyword, Pageable pageable) {
}
