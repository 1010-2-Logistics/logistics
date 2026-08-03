package com.logistics.template.presentation.controller.dto.response;

import com.logistics.template.domain.entity.Inventory;
import java.util.UUID;

public record SampleSummaryResponse(UUID sampleId, String name) {

    public static SampleSummaryResponse from(Inventory sample) {
        return new SampleSummaryResponse(sample.getInventoryId(), sample.getName());
    }
}
