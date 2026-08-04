package com.logistics.hub.application.dto.command;

import java.util.UUID;

public record HubUpdateCommand(UUID hubId, String name) {
}
