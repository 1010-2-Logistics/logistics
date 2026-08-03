package com.logistics.template.domain.repository;

import com.logistics.template.domain.entity.Sample;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SampleQueryRepository {

    Optional<Sample> findByIdAndDeletedAtIsNull(UUID sampleId);

    Page<Sample> search(String keyword, Pageable pageable);
}
