package com.logistics.hub.infrastructure.persistence.repository;

import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubQueryRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubQueryRepositoryImpl implements HubQueryRepository {

    private final HubJpaRepository jpaRepository;

    @Override
    public Optional<Hub> findByIdAndDeletedAtIsNull(UUID hubId) {
        return jpaRepository.findByHubIdAndDeletedAtIsNull(hubId);
    }

    @Override
    public Page<Hub> search(String keyword, Pageable pageable) {
        return jpaRepository.search(keyword, pageable);
    }
}
