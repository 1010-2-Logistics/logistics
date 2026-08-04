package com.logistics.order.domain.entity;

import com.logistics.order.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    // 주문생성 전까지 orderId가 없는데 배송 생성 API는 저장 전에 orderId를 요구함
    @Id
    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "start_company_id", nullable = false)
    private UUID startCompanyId;

    @Column(name = "end_company_id", nullable = false)
    private UUID endCompanyId;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "request", nullable = false, length = 500)
    private String request;

    public static Order create(
            UUID orderId,
            UUID deliveryId,
            UUID startCompanyId,
            UUID endCompanyId,
            UUID productId,
            Integer quantity,
            String request
    ) {
        Order order = new Order();
        order.orderId = orderId;
        order.deliveryId = deliveryId;
        order.startCompanyId = startCompanyId;
        order.endCompanyId = endCompanyId;
        order.productId = productId;
        order.quantity = quantity;
        order.request = request;
        order.status = OrderStatus.CREATED;
        return order;
    }

    public void update(
            Integer quantity,
            String request
    ) {
        if (quantity != null) {
            this.quantity = quantity;
        }

        if (request != null && !request.isBlank()) {
            this.request = request;
        }
    }

    public boolean inCanceled() {
        return this.status == OrderStatus.CANCELED;
    }
}
