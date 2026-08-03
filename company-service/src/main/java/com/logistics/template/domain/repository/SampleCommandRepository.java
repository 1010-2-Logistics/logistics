package com.logistics.template.domain.repository;

import com.logistics.template.domain.entity.Sample;
import java.util.Optional;
import java.util.UUID;

public interface SampleCommandRepository {

    Sample save(Sample sample);

    Optional<Sample> findByIdAndDeletedAtIsNull(UUID sampleId);
}
