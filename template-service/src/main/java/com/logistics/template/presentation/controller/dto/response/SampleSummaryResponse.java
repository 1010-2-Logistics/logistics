package com.logistics.template.presentation.controller.dto.response;

import com.logistics.template.domain.entity.Sample;
import java.util.UUID;

public record SampleSummaryResponse(UUID sampleId, String name) {

    public static SampleSummaryResponse from(Sample sample) {
        return new SampleSummaryResponse(sample.getSampleId(), sample.getName());
    }
}
