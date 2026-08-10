package com.logistics.hub.domain.repository;

import com.logistics.hub.domain.entity.Hub;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubQueryRepository {

    Optional<Hub> findByIdAndDeletedAtIsNull(UUID hubId);

    Page<Hub> search(UUID hubId, Pageable pageable);

    Set<UUID> findValidHubIdsIn(List<UUID> hubIds);

    //내부 api 허브 리스트를 받아 허브 정보 반환
    List<Hub> findAllByHubIdInAndDeletedAtIsNull(List<UUID> hubIds);
}
