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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

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

    @Column(name = "request", nullable = false)
    private String request;

    public static Order create(
            UUID endCompanyId,
            UUID productId,
            Integer quantity,
            String request
    ) {
        Order order = new Order();
        order.endCompanyId = endCompanyId;
        order.productId = productId;
        order.quantity = quantity;
        order.request = request;
        order.status = OrderStatus.CREATED;
        return order;
    }
}
