package com.logistics.hub.domain.repository;

import com.logistics.hub.domain.entity.Hub;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubQueryRepository {

    Optional<Hub> findByIdAndDeletedAtIsNull(UUID hubId);

    Page<Hub> search(UUID hubId, Pageable pageable);
}
