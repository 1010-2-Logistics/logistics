package com.logistics.hub.infrastructure.persistence.repository;

import com.logistics.hub.domain.entity.Hub;
import com.logistics.hub.domain.repository.HubCommandRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubCommandRepositoryImpl implements HubCommandRepository {

    private final HubJpaRepository jpaRepository;

    @Override
    public Hub save(Hub hub) {
        return jpaRepository.save(hub);
    }

    @Override
    public Optional<Hub> findByIdAndDeletedAtIsNull(UUID hubId) {
        return jpaRepository.findByHubIdAndDeletedAtIsNull(hubId);
    }

    @Override
    public boolean existsByLatitudeAndLongitudeAndDeletedAtIsNull(BigDecimal latitude, BigDecimal longitude) {
        return jpaRepository.existsByLatitudeAndLongitudeAndDeletedAtIsNull(latitude, longitude);
    }

    @Override
    public boolean existsByHubAddressAndDeletedAtIsNull(String hubAddress) {
        return jpaRepository.existsByHubAddressAndDeletedAtIsNull(hubAddress);
    }
}
