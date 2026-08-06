package com.logistics.delivery.application.dto.query;

import com.logistics.delivery.domain.entity.DeliveryStatus;
import java.util.UUID;

public record SearchDeliveryQuery(DeliveryStatus status, UUID hubId, String sort, int page, int size) {

    private static final int[] ALLOWED_SIZES = {10, 30, 50};

    public static SearchDeliveryQuery of(DeliveryStatus status, UUID hubId, String sort, Integer page, Integer size) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = normalizeSize(size);
        String safeSort = (sort == null || sort.isBlank()) ? "createdAt" : sort;
        return new SearchDeliveryQuery(status, hubId, safeSort, safePage, safeSize);
    }

    private static int normalizeSize(Integer size) {
        if (size == null) {
            return 10;
        }
        for (int allowed : ALLOWED_SIZES) {
            if (allowed == size) {
                return size;
            }
        }
        return 10;
    }
}