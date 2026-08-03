package com.logistics.inventory.domain.entity;

import com.logistics.inventory.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "p_inventory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_id", nullable = false)
    private UUID inventoryId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    public static Inventory create(
            UUID productId,
            UUID hubId,
            Integer stock
    ) {
        Inventory inventory = new Inventory();
        inventory.productId = productId;
        inventory.hubId = hubId;
        inventory.stock = stock;

        return inventory;
    }

    public void updateStock(
            Integer stock
    ) {
        this.stock = stock;
    }
}
