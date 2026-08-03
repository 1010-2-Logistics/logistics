package com.logistics.hub.domain.repository;

import com.logistics.hub.domain.entity.Hub;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubQueryRepository {

    Optional<Hub> findByIdAndDeletedAtIsNull(UUID sampleId);

    Page<Hub> search(String keyword, Pageable pageable);
}
