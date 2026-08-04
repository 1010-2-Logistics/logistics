package com.logistics.delivery.application.dto.query;

import com.logistics.delivery.domain.entity.ManagerType;
import java.util.UUID;

public record SearchDeliveryManagerQuery(ManagerType managerType, UUID hubId, int page, int size) {

    private static final int[] ALLOWED_SIZES = {10, 30, 50};

    public static SearchDeliveryManagerQuery of(ManagerType managerType, UUID hubId, Integer page, Integer size) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = normalizeSize(size);
        return new SearchDeliveryManagerQuery(managerType, hubId, safePage, safeSize);
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