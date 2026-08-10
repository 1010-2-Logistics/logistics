package com.logistics.delivery.application.service;

import com.logistics.delivery.application.dto.query.SearchDeliveryQuery;
import com.logistics.delivery.application.dto.result.DeliveryResults;
import com.logistics.delivery.application.dto.result.DeliveryResults.DeliveryDetailResult;
import com.logistics.delivery.domain.entity.*;
import com.logistics.delivery.domain.repository.DeliveryRepository;
import com.logistics.delivery.domain.repository.DeliveryRouteRepository;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.logistics.delivery.infrastructure.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryQueryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryDetailResult getById(UUID deliveryId, UserPrincipal principal) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
        List<DeliveryRoute> routes = deliveryRouteRepository.findAllByDeliveryId(deliveryId);
        validateOwnership(principal, delivery, routes);
        return new DeliveryDetailResult(delivery);
    }

    public Page<DeliveryDetailResult> search(SearchDeliveryQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, query.sort()));
        return deliveryRepository.search(query.status(), query.hubId(), pageRequest).map(DeliveryDetailResult::new);
    }

    private final DeliveryRouteRepository deliveryRouteRepository;

    public DeliveryResults.DeliveryRouteListResult getRoutes(UUID deliveryId, UserPrincipal principal) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
        List<DeliveryRoute> routes = deliveryRouteRepository.findAllByDeliveryId(deliveryId);
        validateOwnership(principal, delivery, routes);
        return new DeliveryResults.DeliveryRouteListResult(routes);
    }

    public DeliveryResults.DeliveryRouteListResult getRoutesInternal(UUID deliveryId) {
        deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
        List<DeliveryRoute> routes = deliveryRouteRepository.findAllByDeliveryId(deliveryId);
        return new DeliveryResults.DeliveryRouteListResult(routes);
    }

    public DeliveryResults.DeliveryInternalResult getInternal(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
        List<DeliveryRoute> routes = deliveryRouteRepository.findAllByDeliveryId(deliveryId);
        Long currentManagerId = resolveCurrentManagerId(delivery, routes);
        return new DeliveryResults.DeliveryInternalResult(delivery, currentManagerId);
    }

    private Long resolveCurrentManagerId(Delivery delivery, List<DeliveryRoute> routes) {
        if (delivery.getStatus() == DeliveryStatus.COMPANY_MOVING || delivery.getStatus() == DeliveryStatus.DELIVERED) {
            return delivery.getCompanyDeliveryManagerId();
        }
        if (delivery.getStatus() == DeliveryStatus.CANCELLED) {
            return null;
        }
        return routes.stream()
                .filter(r -> r.getStatus() != DeliveryRouteStatus.DEST_HUB_ARRIVED)
                .min(Comparator.comparingInt(DeliveryRoute::getSequence))
                .map(DeliveryRoute::getDeliveryManagerId)
                .orElse(null);
    }

    private void validateOwnership(UserPrincipal principal, Delivery delivery, List<DeliveryRoute> routes) {
        if (principal.getRole() == Role.MASTER || principal.getRole() == Role.COMPANY_MANAGER) {
            return; // COMPANY_MANAGER는 order-service 확인이 별도 이슈라 일단 통과
        }
        boolean owns = switch (principal.getRole()) {
            case HUB_MANAGER -> principal.getHubId().equals(delivery.getStartHubId())
                    || principal.getHubId().equals(delivery.getEndHubId());
            case COMPANY_DELIVERY_MANAGER -> principal.getUserId().equals(delivery.getCompanyDeliveryManagerId());
            case HUB_DELIVERY_MANAGER -> routes.stream()
                    .anyMatch(r -> principal.getUserId().equals(r.getDeliveryManagerId()));
            default -> false;
        };
        if (!owns) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
    }
}