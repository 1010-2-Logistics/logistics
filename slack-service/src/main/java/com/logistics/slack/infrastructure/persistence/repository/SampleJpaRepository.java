package com.logistics.slack.infrastructure.persistence.repository;

import com.logistics.slack.domain.entity.Slack;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SampleJpaRepository extends JpaRepository<Slack, UUID> {

    Optional<Slack> findBySampleIdAndDeletedAtIsNull(UUID sampleId);

    @Query("SELECT s FROM Slack s WHERE s.deletedAt IS NULL "
            + "AND (:keyword IS NULL OR s.name LIKE %:keyword%)")
    Page<Slack> search(@Param("keyword") String keyword, Pageable pageable);
}
