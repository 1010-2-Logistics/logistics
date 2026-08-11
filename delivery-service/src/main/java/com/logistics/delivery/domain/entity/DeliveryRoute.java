package com.logistics.delivery.domain.entity;

import com.logistics.delivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_delivery_route")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_route_id")
    private UUID deliveryRouteId;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "start_hub_id", nullable = false)
    private UUID startHubId;

    @Column(name = "end_hub_id", nullable = false)
    private UUID endHubId;

    @Column(name = "delivery_manager_id", nullable = false)
    private Long deliveryManagerId;

    @Column(name = "expected_distance", precision = 10, scale = 2, nullable = false)
    private BigDecimal expectedDistance;

    @Column(name = "expected_duration", nullable = false)
    private Integer expectedDuration;

    @Column(name = "actual_distance", precision = 10, scale = 2)
    private BigDecimal actualDistance;

    @Column(name = "actual_duration")
    private Integer actualDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryRouteStatus status;

    public static DeliveryRoute create(UUID deliveryId, int sequence, UUID startHubId, UUID endHubId,
                                       Long deliveryManagerId, BigDecimal expectedDistance, Integer expectedDuration) {
        DeliveryRoute route = new DeliveryRoute();
        route.deliveryId = deliveryId;
        route.sequence = sequence;
        route.startHubId = startHubId;
        route.endHubId = endHubId;
        route.deliveryManagerId = deliveryManagerId;
        route.expectedDistance = expectedDistance;
        route.expectedDuration = expectedDuration;
        route.status = DeliveryRouteStatus.HUB_MOVE_WAITING;
        return route;
    }

    public void recordActual(BigDecimal actualDistance, Integer actualDuration) {
        this.actualDistance = actualDistance;
        this.actualDuration = actualDuration;
    }

    public void changeStatus(DeliveryRouteStatus status) {
        this.status = status;
    }
}