package com.logistics.delivery.domain.entity;

import com.logistics.delivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_delivery")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "start_hub_id", nullable = false)
    private UUID startHubId;

    @Column(name = "end_hub_id", nullable = false)
    private UUID endHubId;

    @Column(name = "company_delivery_manager_id")
    private Long companyDeliveryManagerId; // 목적지 허브 도착 전까지 NULL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "slack_id", nullable = false)
    private String slackId;

    public static Delivery create(UUID orderId, UUID startHubId, UUID endHubId,
                                  String deliveryAddress, String receiverName, String slackId, Long createdBy) {
        Delivery delivery = new Delivery();
        delivery.orderId = orderId;
        delivery.startHubId = startHubId;
        delivery.endHubId = endHubId;
        delivery.deliveryAddress = deliveryAddress;
        delivery.receiverName = receiverName;
        delivery.slackId = slackId;
        delivery.status = DeliveryStatus.HUB_WAITING;
        delivery.setCreatedBy(createdBy);
        return delivery;
    }

    public void assignCompanyDeliveryManager(Long deliveryManagerId) {
        this.companyDeliveryManagerId = deliveryManagerId;
    }

    public void changeStatus(DeliveryStatus status) {
        this.status = status;
    }
}