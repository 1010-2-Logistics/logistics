package com.logistics.delivery.application.event;

import java.util.UUID;

public record SampleCreatedEvent(UUID sampleId, String name) {
}
