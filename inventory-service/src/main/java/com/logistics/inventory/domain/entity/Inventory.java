package com.logistics.inventory.domain.entity;

import com.logistics.inventory.global.entity.BaseEntity;
import com.logistics.inventory.global.exception.CustomException;
import com.logistics.inventory.global.exception.InventoryErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
        inventory.updateStock(stock);

        return inventory;
    }

    public void deduct(Integer quantity) {
        validateDeductionQuantity(quantity);
        validateStockAvailability(quantity);

        this.stock -= quantity;
    }

    public void restore(Integer quantity) {
        validateRestorationQuantity(quantity);

        this.stock += quantity;
    }

    private void validateDeductionQuantity(Integer quantity) {
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

    public void delete(Long deletedBy) {
        markDeleted(deletedBy);
    }

    // 다른 경로오 오는 경우는 대비함
    public void updateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new CustomException(
                    InventoryErrorCode.INVENTORY_INVALID_REQUEST
            );
        }

        this.stock = stock;
    }
}
