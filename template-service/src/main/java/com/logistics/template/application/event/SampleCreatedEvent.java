package com.logistics.template.application.event;

import java.util.UUID;

public record SampleCreatedEvent(UUID sampleId, String name) {
}
