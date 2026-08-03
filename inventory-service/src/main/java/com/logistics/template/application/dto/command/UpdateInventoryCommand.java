package com.logistics.template.application.dto.command;

import java.util.UUID;

public record UpdateInventoryCommand(UUID sampleId, String name) {
}
