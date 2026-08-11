package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.command.ChangeDeliveryRouteStatusCommand;
import com.logistics.delivery.application.dto.command.ChangeDeliveryStatusCommand;
import com.logistics.delivery.application.dto.command.CreateDeliveryCommand;
import com.logistics.delivery.application.dto.result.DeliveryResults.DeliveryCreateResult;
import com.logistics.delivery.application.dto.result.DeliveryResults.DeliveryDetailResult;
import com.logistics.delivery.application.dto.result.DeliveryResults.RouteStatusChangeResult;
import com.logistics.delivery.application.port.HubPort;
import com.logistics.delivery.application.port.HubRoutePort;
import com.logistics.delivery.domain.entity.*;
import com.logistics.delivery.domain.repository.DeliveryManagerRepository;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
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

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DeliveryManagerAssignmentService deliveryManagerAssignmentService;
    private final HubPort hubPort;
    private final HubRoutePort hubRoutePort;
    private final DeliveryManagerRepository deliveryManagerRepository;

    public DeliveryCreateResult createDelivery(CreateDeliveryCommand command) {
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
                command.deliveryAddress(), command.receiverName(), command.slackId(),
                command.startCompanyId(), command.endCompanyId());
        Delivery savedDelivery = deliveryRepository.save(delivery);

        List<RouteSegment> segments = resolveRouteSegments(command.startHubId(), command.endHubId());

        int sequence = 0;
        for (RouteSegment segment : segments) {
            DeliveryManager manager = deliveryManagerAssignmentService
                    .assignNextManager(ManagerType.HUB_DELIVERY_MANAGER, null);

            DeliveryRoute route = DeliveryRoute.create(
                    savedDelivery.getDeliveryId(), sequence++, segment.startHubId(), segment.endHubId(),
                    manager.getDeliveryManagerId(), segment.expectedDistance(), segment.expectedDuration());
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
            throw new CustomException(DeliveryErrorCode.DELIVERY_HUB_SERVICE_UNAVAILABLE);
        }
    }

    private List<RouteSegment> resolveRouteSegments(UUID startHubId, UUID endHubId) {
        try {
            return hubRoutePort.findRoute(startHubId, endHubId).stream()
                    .map(seg -> new RouteSegment(seg.startHubId(), seg.endHubId(), seg.distance(), seg.duration()))
                    .toList();
        } catch (FeignException.NotFound e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_HUB_ROUTE_NOT_FOUND);
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_HUB_ROUTE_SERVICE_UNAVAILABLE);
        }
    }

    private record RouteSegment(UUID startHubId, UUID endHubId, BigDecimal expectedDistance, Integer expectedDuration) {
    }

    public DeliveryDetailResult changeDeliveryStatus(UUID deliveryId, ChangeDeliveryStatusCommand command, UserPrincipal principal) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        validateDeliveryOwnership(principal, delivery);

        if (command.status() != DeliveryStatus.DELIVERED || delivery.getStatus() != DeliveryStatus.COMPANY_MOVING) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
        }

        delivery.changeStatus(DeliveryStatus.DELIVERED);
        return new DeliveryDetailResult(delivery);
    }

    // MASTER 전체, COMPANY_DELIVERY_MANAGER는 본인 배송만
    private void validateDeliveryOwnership(UserPrincipal principal, Delivery delivery) {
        if (principal.getRole() == Role.MASTER) {
            return;
        }
        if (principal.getRole() == Role.COMPANY_DELIVERY_MANAGER
                && principal.getUserId().equals(delivery.getCompanyDeliveryManagerId())) {
            validateActiveManager(principal.getUserId());
            return;
        }
        throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    public RouteStatusChangeResult changeDeliveryRouteStatus(UUID deliveryId, UUID routeId,
                                                     ChangeDeliveryRouteStatusCommand command, UserPrincipal principal) {
        DeliveryRoute route = deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_ROUTE_NOT_FOUND));
        if (!route.getDeliveryId().equals(deliveryId)) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_ROUTE_NOT_FOUND);
        }
        validateRouteOwnership(principal, route);
        validateRouteTransition(route.getStatus(), command.status());
        validateSequentialProgress(deliveryId, route.getSequence());

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

    private void validateSequentialProgress(UUID deliveryId, int sequence) {
        if (sequence == 0) {
            return;
        }
        boolean allPriorCompleted = deliveryRouteRepository.findAllByDeliveryId(deliveryId).stream()
                .filter(r -> r.getSequence() < sequence)
                .allMatch(r -> r.getStatus() == DeliveryRouteStatus.DEST_HUB_ARRIVED);
        if (!allPriorCompleted) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
        }
    }

    public void deleteDelivery(UUID deliveryId, UserPrincipal principal) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
        validateHubManagerOwnership(principal, delivery);
        delivery.markDeleted(principal.getUserId());
    }

    // MASTER 전체, HUB_MANAGER는 담당 허브 소속만
    private void validateHubManagerOwnership(UserPrincipal principal, Delivery delivery) {
        if (principal.getRole() == Role.MASTER) {
            return;
        }
        if (principal.getRole() == Role.HUB_MANAGER
                && (principal.getHubId().equals(delivery.getStartHubId())
                        || principal.getHubId().equals(delivery.getEndHubId()))) {
            return;
        }
        throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    public void cancelDelivery(UUID orderId) {
        Optional<Delivery> delivery = deliveryRepository.findByOrderId(orderId);
        if (delivery.isEmpty()) {
            return; // 이미 없음 = 목표 상태 달성, 그냥 성공 처리
        }
        Delivery found = delivery.get();
        if (found.getStatus() == DeliveryStatus.DELIVERED) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
        }
        found.changeStatus(DeliveryStatus.CANCELLED);
    }

    private void validateRouteOwnership(UserPrincipal principal, DeliveryRoute route) {
        if (principal.getRole() == Role.MASTER) {
            return;
        }
        boolean owns = switch (principal.getRole()) {
            case HUB_MANAGER -> principal.getHubId().equals(route.getStartHubId())
                    || principal.getHubId().equals(route.getEndHubId());
            case HUB_DELIVERY_MANAGER, COMPANY_DELIVERY_MANAGER -> {
                validateActiveManager(principal.getUserId());
                yield principal.getUserId().equals(route.getDeliveryManagerId());
            }

            default -> false;
        };
        if (!owns) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }

    }

    // 해제된 담당자는 경로에 ID가 남아있어도 접근할 수 없어야 한다
    private void validateActiveManager(Long userId) {
        deliveryManagerRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN));
    }
}
