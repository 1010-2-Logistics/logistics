package com.logistics.template.infrastructure.persistence.repository;

import com.logistics.template.domain.entity.Sample;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SampleJpaRepository extends JpaRepository<Sample, UUID> {

    Optional<Sample> findBySampleIdAndDeletedAtIsNull(UUID sampleId);

    @Query("SELECT s FROM Sample s WHERE s.deletedAt IS NULL "
            + "AND (:keyword IS NULL OR s.name LIKE %:keyword%)")
    Page<Sample> search(@Param("keyword") String keyword, Pageable pageable);
}
