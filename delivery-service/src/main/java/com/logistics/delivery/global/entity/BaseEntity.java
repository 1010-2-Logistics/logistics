package com.logistics.delivery.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Long updatedBy;

    private LocalDateTime deletedAt;

    private Long deletedBy;

    protected void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void markDeleted(Long deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}
