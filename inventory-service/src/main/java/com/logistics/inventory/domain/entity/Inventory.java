package com.logistics.inventory.domain.entity;

import com.logistics.inventory.global.entity.BaseEntity;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.global.exception.InventoryErrorCode;
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

    public void deduct(Integer quantity) {
        validDeductionQuantity(quantity);
        validateStockAvailability(quantity);

        this.stock -= quantity;
    }

    public void restore(Integer quantity) {
        validateRestorationQuantity(quantity);

        this.stock += quantity;
    }

    private void validDeductionQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new CustomException(InventoryErrorCode.INVENTORY_INVALID_REQUEST);
        }
    }

    private void validateStockAvailability(Integer quantity) {
        if (stock < quantity) {
            throw new CustomException(InventoryErrorCode.INVENTORY_OUT_OF_STOCK);
        }
    }

    private void validateRestorationQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new CustomException(InventoryErrorCode.INVENTORY_INVALID_REQUEST);
        }
    }

    // TODO : baseEntity
    public void delete(Long deletedBy) {
        markDeleted(deletedBy);
    }
}
