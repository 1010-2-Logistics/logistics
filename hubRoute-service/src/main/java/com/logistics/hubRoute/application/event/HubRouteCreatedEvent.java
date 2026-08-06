package com.logistics.hubRoute.application.event;

import java.util.UUID;

public record HubRouteCreatedEvent(UUID hubRouteId, String name) {
}
