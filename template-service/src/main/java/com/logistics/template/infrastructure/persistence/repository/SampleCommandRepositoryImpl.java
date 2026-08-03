package com.logistics.template.infrastructure.persistence.repository;

import com.logistics.template.domain.entity.Sample;
import com.logistics.template.domain.repository.SampleCommandRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SampleCommandRepositoryImpl implements SampleCommandRepository {

    private final SampleJpaRepository jpaRepository;

    @Override
    public Sample save(Sample sample) {
        return jpaRepository.save(sample);
    }

    @Override
    public Optional<Sample> findByIdAndDeletedAtIsNull(UUID sampleId) {
        return jpaRepository.findBySampleIdAndDeletedAtIsNull(sampleId);
    }
}
