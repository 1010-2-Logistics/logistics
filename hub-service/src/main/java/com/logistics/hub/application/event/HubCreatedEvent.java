package com.logistics.hub.application.event;

import java.util.UUID;

public record HubCreatedEvent(UUID hubId, String name) {
}
