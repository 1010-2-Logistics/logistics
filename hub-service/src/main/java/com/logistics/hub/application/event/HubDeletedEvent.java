package com.logistics.hub.application.event;

import java.util.UUID;

public record HubDeletedEvent(
        UUID hubId,
        Long deletedBy
) {
}
