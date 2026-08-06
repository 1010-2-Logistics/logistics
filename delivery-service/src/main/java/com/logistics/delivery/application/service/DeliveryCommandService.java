package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.command.CreateDeliveryCommand;
import com.logistics.delivery.domain.entity.Delivery;
import com.logistics.delivery.domain.entity.DeliveryManager;
import com.logistics.delivery.domain.entity.DeliveryRoute;
import com.logistics.delivery.domain.entity.ManagerType;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryCommandService {

    // hub-route 서비스 연동 전까지 쓰는 임시값 (expected_distance > 0 제약 때문에 0은 못 씀)
    private static final BigDecimal PLACEHOLDER_DISTANCE = BigDecimal.ONE;
    private static final int PLACEHOLDER_DURATION = 1;

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DeliveryManagerAssignmentService deliveryManagerAssignmentService;
    // TODO: 인증 붙으면 실제 로그인 사용자(호출자)로 교체
    private static final Long TEMP_CREATED_BY = 1L;

    public Delivery create(CreateDeliveryCommand command) {
        Delivery delivery = Delivery.create(
                command.orderId(), command.startHubId(), command.endHubId(),
                command.deliveryAddress(), command.receiverName(), command.slackId(), TEMP_CREATED_BY);
        Delivery savedDelivery = deliveryRepository.save(delivery);

        List<RouteSegment> segments = resolveRouteSegments(command.startHubId(), command.endHubId());

        int sequence = 0;
        for (RouteSegment segment : segments) {
            DeliveryManager manager = deliveryManagerAssignmentService
                    .assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null);

            DeliveryRoute route = DeliveryRoute.create(
                    savedDelivery.getDeliveryId(), sequence++, segment.startHubId(), segment.endHubId(),
                    manager.getDeliveryManagerId(), segment.expectedDistance(), segment.expectedDuration(),
                    TEMP_CREATED_BY);
            deliveryRouteRepository.save(route);
        }

        return savedDelivery;
    }

    // TODO: hub-route 서비스 연동되면 실제 멀티홉 경로 계산으로 교체
    private List<RouteSegment> resolveRouteSegments(UUID startHubId, UUID endHubId) {
        return List.of(new RouteSegment(startHubId, endHubId, PLACEHOLDER_DISTANCE, PLACEHOLDER_DURATION));
    }

    private record RouteSegment(UUID startHubId, UUID endHubId, BigDecimal expectedDistance, Integer expectedDuration) {
    }
}