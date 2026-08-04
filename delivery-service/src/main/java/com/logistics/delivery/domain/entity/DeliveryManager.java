package com.logistics.delivery.domain.entity;

import com.logistics.delivery.global.entity.BaseEntity;
import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_delivery_manager")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryManager extends BaseEntity {

    @Id
    @Column(name = "delivery_manager_id")
    private Long deliveryManagerId; // User 서비스 user_id와 동일, DB 자동생성 아님

    @Column(name = "hub_id")
    private UUID hubId; // 업체 담당자는 필수, 허브 담당자는 NULL

    @Column(name = "slack_id", nullable = false)
    private String slackId;

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_type", nullable = false)
    private ManagerType managerType;

    @Column(name = "delivery_sequence", nullable = false)
    private Integer deliverySequence;

    public static DeliveryManager create(Long deliveryManagerId, UUID hubId, String slackId,
                                         ManagerType managerType, int deliverySequence) {
        validateHubId(managerType, hubId);
        DeliveryManager manager = new DeliveryManager();
        manager.deliveryManagerId = deliveryManagerId;
        manager.hubId = hubId;
        manager.slackId = slackId;
        manager.managerType = managerType;
        manager.deliverySequence = deliverySequence;
        return manager;
    }

    private static void validateHubId(ManagerType managerType, UUID hubId) {
        if (managerType == ManagerType.COMPANY_DELIVERY_MANAGER && hubId == null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_TYPE_HUB_MISMATCH);
        }
        if (managerType == ManagerType.HUB_DELIVERY_MANAGER && hubId != null) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_MANAGER_TYPE_HUB_MISMATCH);
        }
    }
}