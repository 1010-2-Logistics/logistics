package com.logistics.template.domain.entity;

import com.logistics.template.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 실제 서비스로 복사할 때: Sample -> 도메인 엔티티명, p_sample -> p_{테이블명}으로 바꾸세요.
@Getter
@Entity
@Table(name = "p_sample")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sample extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sample_id")
    private UUID sampleId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SampleStatus status;

    public static Sample create(String name) {
        Sample sample = new Sample();
        sample.name = name;
        sample.status = SampleStatus.ACTIVE;
        return sample;
    }

    public void update(String name) {
        this.name = name;
    }

    public void changeStatus(SampleStatus status) {
        this.status = status;
    }
}
