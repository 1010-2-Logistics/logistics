package com.logistics.hub.application.dto.command;

import java.util.UUID;

public record UpdateHubCommand(UUID sampleId, String name) {
}
