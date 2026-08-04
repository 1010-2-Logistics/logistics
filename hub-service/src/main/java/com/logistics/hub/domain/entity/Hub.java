package com.logistics.hub.domain.entity;

import com.logistics.hub.global.entity.BaseEntity;
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
@Table(name = "p_hub")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hub_id")
    private UUID hubId;

    @Column(name = "hub_name", nullable = false)
    private String hubName;

    @Column(name = "hub_address", nullable = false)
    private String hubAddress;

    @Column(name = "latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal longitude;

    // ※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※
    // ※※※※※※※※※임시 코드, 중복인거 앎, 포스트맨에서 테스트 할 경우 유저아이디가 없어서 못하는것 방지하기 위함※※※※※※※※※
    // ※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※※
    private Long createdBy;


    public static Hub create(String hubName, String hubAddress, BigDecimal latitude, BigDecimal longitude, Long createdBy) {
        Hub hub = new Hub();
        hub.hubName = hubName;
        hub.hubAddress = hubAddress;
        hub.latitude = latitude;
        hub.longitude = longitude;
        hub.createdBy = createdBy;
        return hub;
    }

    public void update(String hubName) {
        this.hubName = hubName;
    }


}
