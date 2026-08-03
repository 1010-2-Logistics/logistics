package com.logistics.template.infrastructure.persistence.repository;

import com.logistics.template.domain.entity.Sample;
import com.logistics.template.domain.repository.SampleQueryRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SampleQueryRepositoryImpl implements SampleQueryRepository {

    private final SampleJpaRepository jpaRepository;

    @Override
    public Optional<Sample> findByIdAndDeletedAtIsNull(UUID sampleId) {
        return jpaRepository.findBySampleIdAndDeletedAtIsNull(sampleId);
    }

    @Override
    public Page<Sample> search(String keyword, Pageable pageable) {
        return jpaRepository.search(keyword, pageable);
    }
}
