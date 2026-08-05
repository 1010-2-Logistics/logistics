package com.logistics.slack.application.event;

import java.util.UUID;

public record SampleCreatedEvent(UUID sampleId, String name) {
}
