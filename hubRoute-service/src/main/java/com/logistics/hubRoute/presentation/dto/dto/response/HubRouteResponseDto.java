package com.logistics.hubRoute.presentation.dto.dto.response;

import com.logistics.hubRoute.domain.entity.HubRoute;

import java.math.BigDecimal;
import java.util.UUID;

public record HubRouteResponseDto(UUID hubRouteId,
                                  UUID startHubId,
                                  UUID endHubId,
                                  Integer duration,
                                  String formattedDuration,
                                  BigDecimal distance
) {

    public static HubRouteResponseDto from(HubRoute hubRoute) {
        return new HubRouteResponseDto(
                hubRoute.getHubRouteId(),
                hubRoute.getStartHubId(),
                hubRoute.getEndHubId(),
                hubRoute.getDuration(),
                formatDuration(hubRoute.getDuration()),
                hubRoute.getDistance()
        );
    }

    private static String formatDuration(Integer minutes) {
        if (minutes == null) {
            return "0분";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours == 0) {
            return remainingMinutes + "분";
        }
        if (remainingMinutes == 0) {
            return hours + "시간";
        }
        return hours + "시간 " + remainingMinutes + "분";
    }
}