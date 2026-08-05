package com.logistics.delivery.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_delivery_manager_assignment_state")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryManagerAssignmentState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_state_id")
    private Long assignmentStateId;

    @Column(name = "hub_id")
    private UUID hubId; // 업체 담당자 풀은 허브별, 허브 담당자 풀은 전역이라 NULL

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_type", nullable = false)
    private ManagerType managerType;

    @Column(name = "last_assigned_sequence", nullable = false)
    private Integer lastAssignedSequence;

    @Column(name = "last_assigned_manager_id")
    private Long lastAssignedManagerId;

    public static DeliveryManagerAssignmentState init(ManagerType managerType, UUID hubId) {
        DeliveryManagerAssignmentState state = new DeliveryManagerAssignmentState();
        state.managerType = managerType;
        state.hubId = hubId;
        state.lastAssignedSequence = -1; // 첫 조회에서 시퀀스 0번이 자연스럽게 잡히도록
        return state;
    }

    public void assign(DeliveryManager manager) {
        this.lastAssignedSequence = manager.getDeliverySequence();
        this.lastAssignedManagerId = manager.getDeliveryManagerId();
    }
}
