package com.logistics.order.integration.outbox;


import com.logistics.order.application.dto.command.OrderCreateCommand;
import com.logistics.order.application.service.OrderCommandService;
import com.logistics.order.domain.entity.OutboxEvent;
import com.logistics.order.domain.repository.OutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class OrderOutboxTransactionIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
            .withCopyFileToContainer(MountableFile.forHostPath(
                            Path.of(
                                    "..",
                                    "infra",
                                    "postgres",
                                    "order-inventory-slack.sql"
                            ).toAbsolutePath().normalize()),
                    "/docker-entrypoint-initdb.d/order-inventory-slack.sql")
            .withUrlParam(
                    "currentSchema",
                    "order_service"
            );

    @Autowired
    private OrderCommandService orderCommandService;

    @MockitoBean
    private OutboxRepository outboxRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Outbox 저장 실패 시 같은 트랜잭션의 주문 저장도 롤백된다")
    void rollbackOrder_whenOutboxSaveFails() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        UUID startCompanyId = UUID.randomUUID();
        UUID endCompanyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderCreateCommand command = new OrderCreateCommand(
                endCompanyId,
                productId,
                10,
                "요청사항"
        );

        given(outboxRepository.save(any(OutboxEvent.class))).willThrow(new RuntimeException("Outbox 저장 실패"));

        assertThatThrownBy(() -> orderCommandService.createOrder(
                command,
                orderId,
                deliveryId,
                startCompanyId,
                "receiverName",
                "receiverSlackId"
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Outbox 저장 실패");

        Integer orderCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM order_service.p_order
                        WHERE order_id = ?
                        """,
                Integer.class,
                orderId
        );

        assertThat(orderCount).isZero();
    }
}
