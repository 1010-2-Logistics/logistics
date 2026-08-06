package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.command.ChangeDeliveryRouteStatusCommand;
import com.logistics.delivery.application.dto.command.ChangeDeliveryStatusCommand;
import com.logistics.delivery.application.dto.command.CreateDeliveryCommand;
import com.logistics.delivery.application.dto.result.DeliveryResults.DeliveryCreateResult;
import com.logistics.delivery.application.dto.result.DeliveryResults.DeliveryDetailResult;
import com.logistics.delivery.application.dto.result.DeliveryResults.RouteStatusChangeResult;
import com.logistics.delivery.application.port.HubPort;
import com.logistics.delivery.domain.entity.*;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import feign.FeignException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryCommandService {

    private static final BigDecimal PLACEHOLDER_DISTANCE = BigDecimal.ONE;
    private static final int PLACEHOLDER_DURATION = 1;
    private static final Long TEMP_CREATED_BY = 1L; // TODO: 인증 붙으면 실제 로그인 사용자(호출자)로 교체

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DeliveryManagerAssignmentService deliveryManagerAssignmentService;
    private final HubPort hubPort;

    public DeliveryCreateResult create(CreateDeliveryCommand command) {
        Optional<Delivery> existing = deliveryRepository.findByOrderId(command.orderId());
        if (existing.isPresent()) {
            Delivery delivery = existing.get();
            int routeCount = deliveryRouteRepository.countByDeliveryId(delivery.getDeliveryId());
            return new DeliveryCreateResult(delivery, routeCount);
        }

        validateHub(command.startHubId());
        validateHub(command.endHubId());

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

        return new DeliveryCreateResult(savedDelivery, segments.size());
    }

    private void validateHub(UUID hubId) {
        try {
            Set<UUID> validIds = hubPort.validateHubIds(List.of(hubId));
            if (!validIds.contains(hubId)) {
                throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_HUB_ID);
            }
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }

    // TODO: hub-route 서비스 연동되면 실제 멀티홉 경로 계산으로 교체
    private List<RouteSegment> resolveRouteSegments(UUID startHubId, UUID endHubId) {
        return List.of(new RouteSegment(startHubId, endHubId, PLACEHOLDER_DISTANCE, PLACEHOLDER_DURATION));
    }

    private record RouteSegment(UUID startHubId, UUID endHubId, BigDecimal expectedDistance, Integer expectedDuration) {
    }

    public DeliveryDetailResult changeStatus(UUID deliveryId, ChangeDeliveryStatusCommand command) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        if (command.status() != DeliveryStatus.DELIVERED || delivery.getStatus() != DeliveryStatus.COMPANY_MOVING) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
        }

        delivery.changeStatus(DeliveryStatus.DELIVERED);
        return new DeliveryDetailResult(delivery);
    }

    public RouteStatusChangeResult changeRouteStatus(UUID deliveryId, UUID routeId, ChangeDeliveryRouteStatusCommand command) {
        DeliveryRoute route = deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_ROUTE_NOT_FOUND));
        if (!route.getDeliveryId().equals(deliveryId)) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_ROUTE_NOT_FOUND);
        }
        validateRouteTransition(route.getStatus(), command.status());

        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        route.changeStatus(command.status());

        if (command.status() == DeliveryRouteStatus.HUB_MOVING
                && route.getSequence() == 0
                && delivery.getStatus() == DeliveryStatus.HUB_WAITING) {
            delivery.changeStatus(DeliveryStatus.HUB_MOVING);
        }

        if (command.status() == DeliveryRouteStatus.DEST_HUB_ARRIVED) {
            BigDecimal actualDistance = command.actualDistance() != null ? command.actualDistance() : route.getExpectedDistance();
            Integer actualDuration = command.actualDuration() != null ? command.actualDuration() : route.getExpectedDuration();
            route.recordActual(actualDistance, actualDuration);

            int totalRoutes = deliveryRouteRepository.countByDeliveryId(deliveryId);
            boolean isLastRoute = route.getSequence() == totalRoutes - 1;
            if (isLastRoute) {
                DeliveryManager companyManager = deliveryManagerAssignmentService
                        .assignNextManager(ManagerType.COMPANY_DELIVERY_MANAGER, delivery.getEndHubId());
                delivery.assignCompanyDeliveryManager(companyManager.getDeliveryManagerId());
                delivery.changeStatus(DeliveryStatus.COMPANY_MOVING);
            }
        }

        return new RouteStatusChangeResult(route, delivery);
    }

    private void validateRouteTransition(DeliveryRouteStatus current, DeliveryRouteStatus target) {
        boolean valid = (current == DeliveryRouteStatus.HUB_MOVE_WAITING && target == DeliveryRouteStatus.HUB_MOVING)
                || (current == DeliveryRouteStatus.HUB_MOVING && target == DeliveryRouteStatus.DEST_HUB_ARRIVED);
        if (!valid) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
        }
    }
}