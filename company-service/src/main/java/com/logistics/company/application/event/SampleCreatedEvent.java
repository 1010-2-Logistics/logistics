package com.logistics.company.application.event;

import java.util.UUID;

public record SampleCreatedEvent(UUID sampleId, String name) {
}
