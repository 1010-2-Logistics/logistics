package com.logistics.hub.domain.repository;

import com.logistics.hub.domain.entity.Hub;
import java.util.Optional;
import java.util.UUID;

public interface HubCommandRepository {

    Hub save(Hub hub);

    Optional<Hub> findByIdAndDeletedAtIsNull(UUID sampleId);
}
