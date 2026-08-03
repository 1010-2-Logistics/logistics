package com.logistics.template.presentation.controller.dto.response;

import com.logistics.template.domain.entity.Inventory;
import java.util.UUID;

public record SampleResponse(UUID sampleId, String name, String status) {

    public static SampleResponse from(Inventory sample) {
        return new SampleResponse(sample.getInventoryId(), sample.getName(), sample.getStatus().name());
    }
}
