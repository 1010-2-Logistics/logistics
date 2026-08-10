package com.logistics.hubRoute.application.dto.command;

import java.math.BigDecimal;
import java.util.UUID;

public record HubRouteUpdateCommand(
        UUID startHubId,
        UUID endHubId,
        Integer duration,
        BigDecimal distance
) {


}
