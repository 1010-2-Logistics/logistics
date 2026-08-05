package com.logistics.delivery.infrastructure.persistence.repository;

import com.logistics.delivery.domain.entity.DeliveryManagerAssignmentState;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryManagerAssignmentStateRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryManagerAssignmentStateRepositoryImpl implements DeliveryManagerAssignmentStateRepository {

    private final DeliveryManagerAssignmentStateJpaRepository jpaRepository;

    @Override
    public Optional<DeliveryManagerAssignmentState> findForUpdate(ManagerType managerType, UUID hubId) {
        return jpaRepository.findByManagerTypeAndHubId(managerType, hubId);
    }

    @Override
    public DeliveryManagerAssignmentState save(DeliveryManagerAssignmentState state) {
        return jpaRepository.save(state);
    }
}
