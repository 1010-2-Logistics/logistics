package com.logistics.hubRoute.domain.entity;

import com.logistics.hubRoute.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 실제 서비스로 복사할 때: Sample -> 도메인 엔티티명, p_sample -> p_{테이블명}으로 바꾸세요.
@Getter
@Entity
@Table(name = "p_hub_route")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_route_id")
    private UUID hubRouteId;

    @Column(name = "start_hub_id")
    private UUID startHubId;

    @Column(name = "end_hub_id")
    private UUID endHubId;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "distance", precision = 10, scale = 2, nullable = false)
    private BigDecimal distance;



    public static HubRoute create(UUID startHubId, UUID endHubId, Integer duration, BigDecimal distance, Long createdBy) {
        HubRoute hub = new HubRoute();
        hub.startHubId = startHubId;
        hub.endHubId = endHubId;
        hub.duration = duration;
        hub.distance = distance;
        hub.setCreatedBy(createdBy);
        return hub;
    }

    public void update(UUID startHubId, UUID endHubId, Integer duration, BigDecimal distance) {
        this.startHubId = startHubId;
        this.endHubId = endHubId;
        this.duration = duration;
        this.distance = distance;
    }


}
