package com.logistics.template.application.event;

import java.util.UUID;

public record InventoryCreatedEvent(UUID sampleId, String name) {
}
