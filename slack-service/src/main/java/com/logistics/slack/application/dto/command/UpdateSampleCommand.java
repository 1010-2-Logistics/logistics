package com.logistics.slack.application.dto.command;

import java.util.UUID;

public record UpdateSampleCommand(UUID sampleId, String name) {
}
