package com.logistics.template.presentation.controller.dto.response;

import com.logistics.template.domain.entity.Sample;
import java.util.UUID;

public record SampleResponse(UUID sampleId, String name, String status) {

    public static SampleResponse from(Sample sample) {
        return new SampleResponse(sample.getSampleId(), sample.getName(), sample.getStatus().name());
    }
}
